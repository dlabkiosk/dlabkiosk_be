package com.moduletest.deasungkioskbackend.domain.attendance.service;

import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceStudentResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
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
public final class AttendanceAdminService {

    private final StudentRepository studentRepository;
    private final SeatRedisService seatRedisService;
    private final PhoneSubmissionRepository phoneSubmissionRepository;

    public List<AttendanceStudentResponse> findAttendanceList(
            Long storeId, String studentName, String studentNumber,
            String status, Boolean phoneSubmitted) {

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<Object, Object> seatStatusMap = seatRedisService.getSeatStatusMap(storeId);
        Set<Long> phoneSubmittedStudentIds = findPhoneSubmittedStudentIds(storeId);

        return students.stream()
            .map(student -> toResponse(student, seatStatusMap, phoneSubmittedStudentIds))
            .filter(r -> matchesStudentName(r, studentName))
            .filter(r -> matchesStudentNumber(r, studentNumber))
            .filter(r -> matchesStatus(r, status))
            .filter(r -> matchesPhoneSubmitted(r, phoneSubmitted))
            .toList();
    }

    private AttendanceStudentResponse toResponse(Student student,
                                                  Map<Object, Object> seatStatusMap,
                                                  Set<Long> phoneSubmittedStudentIds) {
        String seatLabel = student.getAssignedSeat() != null
            ? student.getAssignedSeat().getSeatLabel() : null;

        String attendanceStatus = resolveAttendanceStatus(student, seatStatusMap);
        boolean isPhoneSubmitted = phoneSubmittedStudentIds.contains(student.getId());

        return AttendanceStudentResponse.builder()
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(seatLabel)
            .attendanceStatus(attendanceStatus)
            .phoneSubmitted(isPhoneSubmitted)
            .build();
    }

    private String resolveAttendanceStatus(Student student,
                                            Map<Object, Object> seatStatusMap) {
        if (student.getAssignedSeat() == null) {
            return "미출석";
        }

        String seatId = student.getAssignedSeat().getId().toString();
        String value = (String) seatStatusMap.get(seatId);

        if (value == null || "AVAILABLE".equals(value)) {
            return "미출석";
        }

        if (value.startsWith("IN_USE:")) {
            return "등원";
        }
        if (value.startsWith("OUTING:")) {
            return "외출";
        }
        if (value.startsWith("AWAY:")) {
            return "이탈";
        }

        return "미출석";
    }

    private Set<Long> findPhoneSubmittedStudentIds(Long storeId) {
        LocalDate today = LocalDate.now();
        return phoneSubmissionRepository
            .findAllByStoreIdToday(storeId, today.atStartOfDay())
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
