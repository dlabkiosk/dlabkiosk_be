package com.moduletest.deasungkioskbackend.domain.attendance.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckInRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckOutRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import com.moduletest.deasungkioskbackend.domain.attendance.exception.AttendanceException;
import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final StudentResolverService studentResolverService;

    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.identifier(), request.studentNumber(), request.phone());

        validateStudentStore(student, storeId);

        // 동시 등원 방지: Student 행에 비관적 락
        studentRepository.findByIdForUpdate(student.getId());

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean alreadyCheckedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .isPresent();

        if (alreadyCheckedIn) {
            throw new AttendanceException(ErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.builder()
            .student(student)
            .store(student.getStore())
            .checkInAt(LocalDateTime.now())
            .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(savedAttendance);
    }

    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.identifier(), request.studentNumber(), request.phone());

        validateStudentStore(student, storeId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Attendance attendance = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .orElseThrow(() -> new AttendanceException(ErrorCode.NOT_CHECKED_IN));

        attendance.checkOut(LocalDateTime.now());
        return AttendanceResponse.fromEntity(attendance);
    }

    private void validateStudentStore(Student student, Long storeId) {
        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }
    }
}
