package com.moduletest.deasungkioskbackend.domain.attendance.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceStudentResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceAdminService {

    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final AttendanceRepository attendanceRepository;
    private final DsaAreaService dsaAreaService;
    private final DsaStudentService dsaStudentService;
    private final PhoneSubmissionRepository phoneSubmissionRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;

    public List<AttendanceStudentResponse> findAttendanceList(
            Long storeId, String studentName, String studentNumber,
            String status, Boolean phoneSubmitted) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, String> rfidToSeatCd = buildDsaSeatAssignmentMap(store);
        Map<String, SeatStatusResponse> seatCdToInfo = buildSeatInfoMap(store);
        Set<Long> phoneSubmittedStudentIds = findPhoneSubmittedStudentIds(storeId);
        Map<Long, LocalDateTime> checkedInAtMap = buildCheckedInAtMap(storeId);

        return students.stream()
            .map(student -> toResponse(student, rfidToSeatCd, seatCdToInfo,
                phoneSubmittedStudentIds, checkedInAtMap))
            .filter(r -> matchesStudentName(r, studentName))
            .filter(r -> matchesStudentNumber(r, studentNumber))
            .filter(r -> matchesStatus(r, status))
            .filter(r -> matchesPhoneSubmitted(r, phoneSubmitted))
            .toList();
    }

    @Transactional
    public void updateCheckInTime(Long studentId, LocalDateTime checkInAt) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        validateStoreAccess(student);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                studentId, startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .ifPresentOrElse(
                attendance -> attendance.updateCheckInAt(checkInAt),
                () -> {
                    Attendance newAttendance = Attendance.builder()
                        .student(student)
                        .store(student.getStore())
                        .checkInAt(checkInAt)
                        .build();
                    attendanceRepository.save(newAttendance);
                }
            );
    }

    /**
     * 하원/조퇴 시각 수정 또는 하원 취소.
     * request.checkOutAt == null 이면 하원 취소(등원 상태로 복원).
     */
    @Transactional
    public void updateCheckOut(Long studentId, LocalDateTime checkOutAt, String action) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        validateStoreAccess(student);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Attendance> checkedOutList = attendanceRepository
            .findLatestCheckedOutToday(studentId, startOfDay, endOfDay);
        if (checkedOutList.isEmpty()) {
            throw new BusinessException(ErrorCode.ATTENDANCE_NOT_CHECKED_OUT);
        }
        Attendance target = checkedOutList.get(0);

        if (checkOutAt == null) {
            cancelCheckOut(student, target, startOfDay, endOfDay);
        } else {
            editCheckOutTime(target, checkOutAt, action);
        }
    }

    private void cancelCheckOut(Student student, Attendance target,
                                 LocalDateTime startOfDay, LocalDateTime endOfDay) {
        boolean hasOtherCheckedIn = attendanceRepository.existsOtherCheckedInToday(
            student.getId(), startOfDay, endOfDay, target.getId());
        if (hasOtherCheckedIn) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACTIVE_CHECK_IN);
        }

        target.cancelCheckOut();

        restoreSeatUsageAndRedis(student, startOfDay, endOfDay);
    }

    private void editCheckOutTime(Attendance target, LocalDateTime checkOutAt, String action) {
        if (checkOutAt.isBefore(target.getCheckInAt())) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_OUT_TIME);
        }
        target.updateCheckOut(checkOutAt, action);
    }

    private void restoreSeatUsageAndRedis(Student student,
                                           LocalDateTime startOfDay,
                                           LocalDateTime endOfDay) {
        List<SeatUsage> ended = seatUsageRepository
            .findEndedTodayByStudentId(student.getId(), startOfDay, endOfDay);
        if (ended.isEmpty()) {
            log.warn("하원 취소 — 복원할 SeatUsage(ENDED) 없음. studentId: {}", student.getId());
            return;
        }
        SeatUsage usage = ended.get(0);
        usage.restoreUsage();
        seatRedisService.markSeatInUse(
            student.getStore().getId(), usage.getSeat().getId(),
            student.getId(), student.getName());
    }

    private void validateStoreAccess(Student student) {
        if (!SecurityUtil.isAdmin()) {
            Long currentStoreId = SecurityUtil.getCurrentStoreId();
            if (!student.getStore().getId().equals(currentStoreId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    /**
     * DSA 3.7+3.8+3.10 응답을 seatCd → SeatStatusResponse 맵으로 모은다.
     * state 뿐 아니라 seatNm(좌석 표시명)도 함께 조회해서 학생이 실제
     * 앉은 좌석의 라벨을 응답에 쓸 수 있게 한다.
     */
    private Map<String, SeatStatusResponse> buildSeatInfoMap(Store store) {
        Map<String, SeatStatusResponse> seatCdToInfo = new HashMap<>();

        if (!store.hasDsaCredentials()) {
            return seatCdToInfo;
        }

        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        for (AreaResponse area : areas) {
            List<SeatStatusResponse> seats =
                dsaAreaService.findSeatStatusByArea(area.areaCd(), store);
            for (SeatStatusResponse seat : seats) {
                if (seat.seatCd() != null) {
                    seatCdToInfo.put(seat.seatCd(), seat);
                }
            }
        }

        return seatCdToInfo;
    }

    /**
     * DSA 3.20 getStdInfoList 응답을 rfidNo → seatCd 매핑으로 변환.
     * 출결 상태 조회 시 DSA를 source of truth로 사용하기 위함 (우리 DB의
     * student.assignedSeat이 DSA와 어긋나 있을 수 있으므로).
     */
    private Map<String, String> buildDsaSeatAssignmentMap(Store store) {
        if (!store.hasDsaCredentials()) {
            return Map.of();
        }
        List<DsaStudentData> dsaStudents = dsaStudentService.findAllStudents(store);
        Map<String, String> result = new HashMap<>();
        for (DsaStudentData dsa : dsaStudents) {
            if (dsa.rfidNo() == null || dsa.seatCd() == null) {
                continue;
            }
            result.put(dsa.rfidNo(), dsa.seatCd());
        }
        return result;
    }

    private AttendanceStudentResponse toResponse(Student student,
                                                  Map<String, String> rfidToSeatCd,
                                                  Map<String, SeatStatusResponse> seatCdToInfo,
                                                  Set<Long> phoneSubmittedStudentIds,
                                                  Map<Long, LocalDateTime> checkedInAtMap) {
        SeatStatusResponse dsaSeat = findDsaSeat(student, rfidToSeatCd, seatCdToInfo);
        String seatLabel = resolveSeatLabel(student, dsaSeat);
        String attendanceStatus = resolveAttendanceStatus(dsaSeat);
        boolean isPhoneSubmitted = phoneSubmittedStudentIds.contains(student.getId());

        return AttendanceStudentResponse.builder()
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(seatLabel)
            .attendanceStatus(attendanceStatus)
            .checkedInAt(checkedInAtMap.get(student.getId()))
            .phoneSubmitted(isPhoneSubmitted)
            .build();
    }

    private SeatStatusResponse findDsaSeat(Student student,
                                            Map<String, String> rfidToSeatCd,
                                            Map<String, SeatStatusResponse> seatCdToInfo) {
        String rfidUid = student.getRfidUid();
        if (rfidUid == null || rfidUid.isBlank()) {
            return null;
        }
        String seatCd = rfidToSeatCd.get(rfidUid);
        if (seatCd == null) {
            return null;
        }
        return seatCdToInfo.get(seatCd);
    }

    private String resolveSeatLabel(Student student, SeatStatusResponse dsaSeat) {
        if (dsaSeat != null && dsaSeat.seatNm() != null && !dsaSeat.seatNm().isBlank()) {
            return dsaSeat.seatNm();
        }
        return student.getAssignedSeat() != null
            ? student.getAssignedSeat().getSeatLabel() : null;
    }

    private String resolveAttendanceStatus(SeatStatusResponse dsaSeat) {
        if (dsaSeat == null || dsaSeat.state() == null) {
            return "미확인";
        }
        return switch (dsaSeat.state()) {
            case "S" -> "등원";
            case "D" -> "외출";
            case "T" -> "하원";
            case "X" -> "미출석";
            case "B" -> "공석";
            case "E" -> "통로";
            default -> "미확인";
        };
    }

    private Map<Long, LocalDateTime> buildCheckedInAtMap(Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Attendance> attendances = attendanceRepository.findTodayByStoreIdAndStatus(
            storeId, startOfDay, endOfDay, AttendanceStatus.CHECKED_IN);

        return attendances.stream()
            .collect(Collectors.toMap(
                a -> a.getStudent().getId(),
                Attendance::getCheckInAt,
                (existing, replacement) -> existing
            ));
    }

    private Set<Long> findPhoneSubmittedStudentIds(Long storeId) {
        return phoneSubmissionRepository
            .findActiveByStoreIdAndDate(storeId, LocalDate.now())
            .stream()
            .map(ps -> ps.getStudent().getId())
            .collect(Collectors.toSet());
    }

    private boolean matchesStudentName(AttendanceStudentResponse r, String studentName) {
        if (studentName == null || studentName.isBlank()) {
            return true;
        }
        return r.studentName().contains(studentName);
    }

    private boolean matchesStudentNumber(AttendanceStudentResponse r, String studentNumber) {
        if (studentNumber == null || studentNumber.isBlank()) {
            return true;
        }
        return studentNumber.equals(r.studentNumber());
    }

    private boolean matchesStatus(AttendanceStudentResponse r, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return status.equals(r.attendanceStatus());
    }

    private boolean matchesPhoneSubmitted(AttendanceStudentResponse r, Boolean phoneSubmitted) {
        if (phoneSubmitted == null) {
            return true;
        }
        return phoneSubmitted == r.phoneSubmitted();
    }
}
