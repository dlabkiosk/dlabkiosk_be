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
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeRedisService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final StudentResolverService studentResolverService;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;
    private final StudyTimeRedisService studyTimeRedisService;

    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.identifier(), request.studentNumber());

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

        // 배정된 좌석이 있으면 자동 입실
        seatCheckIn(student, storeId);

        return AttendanceResponse.fromEntity(savedAttendance);
    }

    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.identifier(), request.studentNumber());

        validateStudentStore(student, storeId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Attendance attendance = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .orElseThrow(() -> new AttendanceException(ErrorCode.NOT_CHECKED_IN));

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.checkOut(checkOutTime);

        // 사용 중인 좌석이 있으면 자동 퇴실
        seatCheckOut(student, storeId);

        // 순공시간 계산: (하원 - 등원) - 외출시간 - 좌석이탈시간
        long totalMinutes = Duration.between(
            attendance.getCheckInAt(), checkOutTime).toMinutes();
        long deductionMinutes = studyTimeRedisService.getTotalDeduction(
            storeId, student.getId());
        long studyTimeMinutes = Math.max(totalMinutes - deductionMinutes, 0);

        return AttendanceResponse.fromEntityWithStudyTime(attendance, studyTimeMinutes);
    }

    private void seatCheckIn(Student student, Long storeId) {
        Seat seat = student.getAssignedSeat();
        if (seat == null) {
            return;
        }

        // 이미 좌석 사용 중이면 스킵
        if (seatUsageRepository.findByStudentIdAndStatus(
            student.getId(), SeatUsageStatus.IN_USE).isPresent()) {
            return;
        }

        Long seatId = seat.getId();

        // Redis HSETNX로 원자적 선점
        boolean acquired = seatRedisService.tryOccupySeat(
            storeId, seatId, student.getId(), student.getName());

        if (!acquired) {
            String existing = seatRedisService.getSeatStatus(storeId, seatId);
            if (existing != null
                && existing.contains(":" + student.getId() + ":")) {
                log.info("[CheckIn] stale Redis 데이터 정리 후 재선점 (studentId={}, seatId={})",
                    student.getId(), seatId);
                seatRedisService.markSeatInUse(
                    storeId, seatId, student.getId(), student.getName());
            } else {
                log.warn(
                    "[CheckIn] 배정 좌석 선점 실패 (studentId={}, seatId={}, existing={}) — 등원은 정상 처리됨",
                    student.getId(), seatId, existing);
                return;
            }
        }

        // DB 저장 (실패 시 Redis 롤백)
        try {
            SeatUsage seatUsage = SeatUsage.builder()
                .seat(seat)
                .student(student)
                .store(seat.getStore())
                .startedAt(LocalDateTime.now())
                .build();
            seatUsageRepository.save(seatUsage);
        } catch (Exception e) {
            seatRedisService.releaseSeat(storeId, seatId);
            log.warn("[CheckIn] 좌석 DB 저장 실패, Redis 롤백 (studentId={}, seatId={})",
                student.getId(), seatId, e);
        }
    }

    private void seatCheckOut(Student student, Long storeId) {
        seatUsageRepository.findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                usage.endUsage(LocalDateTime.now());
                try {
                    seatRedisService.releaseSeat(storeId, usage.getSeat().getId());
                } catch (Exception e) {
                    log.warn("[CheckOut] Redis 좌석 해제 실패 (seatId={}) — DB는 퇴실 처리됨",
                        usage.getSeat().getId(), e);
                }
            });
    }

    private void validateStudentStore(Student student, Long storeId) {
        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }
    }
}
