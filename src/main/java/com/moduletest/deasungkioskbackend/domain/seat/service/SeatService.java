package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCheckInRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatType;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.attendance.exception.AttendanceException;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;
    private final SeatRedisService seatRedisService;


    public List<SeatStatusResponse> findSeatStatusByStoreId(Long storeId) {
        List<Seat> seats = seatRepository.findAllByStoreId(storeId);
        Map<Object, Object> redisStatus = seatRedisService.getSeatStatusMap(storeId);

        List<SeatStatusResponse> result = new ArrayList<>();

        for (Seat seat : seats) {
            if (!seat.isActive()) {
                continue;
            }

            String status = (String) redisStatus.get(seat.getId().toString());
            boolean available = status == null;
            boolean outing = false;
            Long studentId = null;
            String studentName = null;

            if (status != null && status.startsWith("IN_USE:")) {
                String[] parts = status.split(":", 3);
                studentId = Long.valueOf(parts[1]);
                studentName = parts[2];
            } else if (status != null && status.startsWith("OUTING:")) {
                String[] parts = status.split(":", 3);
                studentId = Long.valueOf(parts[1]);
                studentName = parts[2];
                outing = true;
            }

            result.add(new SeatStatusResponse(
                seat.getId(),
                seat.getSeatLabel(),
                seat.getSeatType().name(),
                seat.getXPos(),
                seat.getYPos(),
                available,
                outing,
                studentId,
                studentName
            ));
        }
        return result;
    }


    @Transactional
    public void seatCheckIn(Long seatId, SeatCheckInRequest request, Long storeId) {
        Student student = resolveStudent(request.qrUuid(), request.rfidUid());

        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }

        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        // 이미 좌석 사용 중인 학생인지 확인
        seatUsageRepository.findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                throw new SeatException(ErrorCode.STUDENT_ALREADY_HAS_SEAT);
            });

        // Redis HSETNX로 원자적 선점
        boolean acquired = seatRedisService.tryOccupySeat(
            seat.getStore().getId(), seatId, student.getId(), student.getName());

        if (!acquired) {
            throw new SeatException(ErrorCode.SEAT_ALREADY_IN_USE);
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
            seatRedisService.releaseSeat(seat.getStore().getId(), seatId);
            throw e;
        }

    }


    @Transactional
    public void seatCheckOut(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        SeatUsage seatUsage = seatUsageRepository
            .findBySeatIdAndStatus(seatId, SeatUsageStatus.IN_USE)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_IN_USE));

        seatUsage.endUsage(LocalDateTime.now());
        seatRedisService.releaseSeat(seat.getStore().getId(), seatId);
    }

    private Student resolveStudent(String qrUuid, String rfidUid) {
        if (qrUuid != null && !qrUuid.isBlank()) {
            return studentRepository.findByQrUuid(qrUuid)
                .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND_BY_QR));
        }
        if (rfidUid != null && !rfidUid.isBlank()) {
            return studentRepository.findByRfidUid(rfidUid)
                .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND_BY_RFID));
        }
        throw new AttendanceException(ErrorCode.INVALID_CHECK_IN_REQUEST);
    }

    // ===== 관리자 API =====

    public List<SeatResponse> findAllSeats(Long storeId) {
        if (storeId != null) {
            return seatRepository.findAllByStoreId(storeId)
                .stream()
                .map(SeatResponse::fromEntity)
                .toList();
        }
        return seatRepository.findAll()
            .stream()
            .map(SeatResponse::fromEntity)
            .toList();
    }

    public SeatResponse findSeatById(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public SeatResponse createSeat(SeatCreateRequest request) {
        Store store = storeRepository.findById(request.storeId())
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        Seat seat = Seat.builder()
            .store(store)
            .seatLabel(request.seatLabel())
            .seatType(SeatType.valueOf(request.seatType()))
            .xPos(request.xPos())
            .yPos(request.yPos())
            .active(true)
            .build();

        Seat savedSeat = seatRepository.save(seat);
        return SeatResponse.fromEntity(savedSeat);
    }

    @Transactional
    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        seat.updateInfo(
            request.seatLabel(),
            SeatType.valueOf(request.seatType()),
            request.xPos(),
            request.yPos(),
            request.active()
        );
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        seatRepository.delete(seat);
    }

}
