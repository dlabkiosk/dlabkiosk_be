package com.moduletest.deasungkioskbackend.domain.attendance.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
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
    private final PhoneSubmissionRepository phoneSubmissionRepository;

    public List<AttendanceStudentResponse> findAttendanceList(
            Long storeId, String studentName, String studentNumber,
            String status, Boolean phoneSubmitted) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, String> seatCdToState = buildSeatStateMap(store);
        Set<Long> phoneSubmittedStudentIds = findPhoneSubmittedStudentIds(storeId);
        Map<Long, LocalDateTime> checkedInAtMap = buildCheckedInAtMap(storeId);

        return students.stream()
            .map(student -> toResponse(student, seatCdToState, phoneSubmittedStudentIds,
                checkedInAtMap))
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

        if (!SecurityUtil.isAdmin()) {
            Long currentStoreId = SecurityUtil.getCurrentStoreId();
            if (!student.getStore().getId().equals(currentStoreId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }

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

    private Map<String, String> buildSeatStateMap(Store store) {
        Map<String, String> seatCdToState = new HashMap<>();

        if (!store.hasDsaCredentials()) {
            return seatCdToState;
        }

        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        for (AreaResponse area : areas) {
            List<SeatStatusResponse> seats =
                dsaAreaService.findSeatStatusByArea(area.areaCd(), store);
            for (SeatStatusResponse seat : seats) {
                if (seat.seatCd() != null) {
                    seatCdToState.put(seat.seatCd(), seat.state());
                }
            }
        }

        return seatCdToState;
    }

    private AttendanceStudentResponse toResponse(Student student,
                                                  Map<String, String> seatCdToState,
                                                  Set<Long> phoneSubmittedStudentIds,
                                                  Map<Long, LocalDateTime> checkedInAtMap) {
        String seatLabel = student.getAssignedSeat() != null
            ? student.getAssignedSeat().getSeatLabel() : null;

        String attendanceStatus = resolveAttendanceStatus(student, seatCdToState);
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

    private String resolveAttendanceStatus(Student student,
                                            Map<String, String> seatCdToState) {
        if (student.getAssignedSeat() == null
                || student.getAssignedSeat().getSeatCd() == null) {
            return "미확인";
        }

        String state = seatCdToState.get(student.getAssignedSeat().getSeatCd());
        if (state == null) {
            return "미확인";
        }

        return switch (state) {
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
