package com.moduletest.deasungkioskbackend.domain.attendance.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceStudentResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
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
    private final DsaAreaService dsaAreaService;
    private final PhoneSubmissionRepository phoneSubmissionRepository;
    private final SeatRedisService seatRedisService;

    public List<AttendanceStudentResponse> findAttendanceList(
            Long storeId, String studentName, String studentNumber,
            String status, Boolean phoneSubmitted) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, String> seatCdToState = buildSeatStateMap(store);
        Set<Long> phoneSubmittedStudentIds = findPhoneSubmittedStudentIds(storeId);
        Set<Long> lateStudentIds = seatRedisService.findLateStudentIds(storeId);

        return students.stream()
            .map(student -> toResponse(student, seatCdToState, phoneSubmittedStudentIds,
                lateStudentIds))
            .filter(r -> matchesStudentName(r, studentName))
            .filter(r -> matchesStudentNumber(r, studentNumber))
            .filter(r -> matchesStatus(r, status))
            .filter(r -> matchesPhoneSubmitted(r, phoneSubmitted))
            .toList();
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
                                                  Set<Long> lateStudentIds) {
        String seatLabel = student.getAssignedSeat() != null
            ? student.getAssignedSeat().getSeatLabel() : null;

        String attendanceStatus = resolveAttendanceStatus(student, seatCdToState);
        boolean isLate = lateStudentIds.contains(student.getId());
        boolean isPhoneSubmitted = phoneSubmittedStudentIds.contains(student.getId());

        return AttendanceStudentResponse.builder()
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(seatLabel)
            .attendanceStatus(attendanceStatus)
            .late(isLate)
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
            case "N" -> "미출석";
            case "B" -> "공석";
            case "E" -> "통로";
            default -> "미확인";
        };
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
