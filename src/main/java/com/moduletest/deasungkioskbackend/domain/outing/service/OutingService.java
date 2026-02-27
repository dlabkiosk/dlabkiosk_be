package com.moduletest.deasungkioskbackend.domain.outing.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingEndRequest;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingResponse;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingStartRequest;
import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import com.moduletest.deasungkioskbackend.domain.outing.exception.OutingException;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
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
public class OutingService {

    private final OutingRepository outingRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final StudentResolverService studentResolverService;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;

    @Transactional
    public OutingResponse startOuting(OutingStartRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.qrUuid(), request.rfidUid(),
            request.studentNumber(), request.phone());
        validateStudentStore(student, storeId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 등원 상태 확인
        boolean isCheckedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .isPresent();

        if (!isCheckedIn) {
            throw new OutingException(ErrorCode.NOT_CHECKED_IN_FOR_OUTING);
        }

        // 이미 외출 중인지 확인
        boolean alreadyOnOuting = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay)
            .isPresent();

        if (alreadyOnOuting) {
            throw new OutingException(ErrorCode.ALREADY_ON_OUTING);
        }

        // 외출 레코드 생성
        Outing outing = Outing.builder()
            .student(student)
            .store(student.getStore())
            .reason(request.reason())
            .startedAt(LocalDateTime.now())
            .build();

        Outing savedOuting = outingRepository.save(outing);

        // 좌석 사용 중이면 Redis 상태를 OUTING으로 변경
        seatUsageRepository.findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                seatRedisService.markSeatOuting(
                    usage.getStore().getId(),
                    usage.getSeat().getId(),
                    student.getId(),
                    student.getName());
            });

        return OutingResponse.fromEntity(savedOuting);
    }

    @Transactional
    public OutingResponse endOuting(OutingEndRequest request, Long storeId) {
        Student student = studentResolverService.resolveStudent(
            request.qrUuid(), request.rfidUid(),
            request.studentNumber(), request.phone());
        validateStudentStore(student, storeId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 진행 중인 외출 찾기
        Outing outing = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay)
            .orElseThrow(() -> new OutingException(ErrorCode.NOT_ON_OUTING));

        outing.endOuting(LocalDateTime.now());

        // 좌석 사용 중이면 Redis 상태를 IN_USE로 복원
        seatUsageRepository.findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                seatRedisService.markSeatInUse(
                    usage.getStore().getId(),
                    usage.getSeat().getId(),
                    student.getId(),
                    student.getName());
            });

        return OutingResponse.fromEntity(outing);
    }

    private void validateStudentStore(Student student, Long storeId) {
        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }
    }
}
