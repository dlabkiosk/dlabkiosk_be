package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatType;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final StoreRepository storeRepository;
    private final SeatRedisService seatRedisService;


    public List<SeatStatusResponse> findSeatStatusByStoreId(Long storeId) {
        List<Seat> seats = seatRepository.findAllByStoreIdWithStore(storeId);
        Map<Object, Object> redisStatus = seatRedisService.getSeatStatusMap(storeId);

        List<SeatStatusResponse> result = new ArrayList<>();

        for (Seat seat : seats) {
            if (!seat.isActive()) {
                continue;
            }

            String status = (String) redisStatus.get(seat.getId().toString());
            boolean available = status == null;
            boolean outing = false;
            boolean away = false;
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
            } else if (status != null && status.startsWith("AWAY:")) {
                String[] parts = status.split(":", 3);
                studentId = Long.valueOf(parts[1]);
                studentName = parts[2];
                away = true;
            }

            result.add(new SeatStatusResponse(
                seat.getId(),
                seat.getSeatLabel(),
                seat.getSeatType().name(),
                seat.getXPos(),
                seat.getYPos(),
                available,
                outing,
                away,
                studentId,
                studentName
            ));
        }
        return result;
    }


    // ===== 관리자 API =====

    public List<SeatResponse> findAllSeats(Long storeId) {
        if (storeId != null) {
            return seatRepository.findAllByStoreIdWithStore(storeId)
                .stream()
                .map(SeatResponse::fromEntity)
                .toList();
        }
        return seatRepository.findAllWithStore()
            .stream()
            .map(SeatResponse::fromEntity)
            .toList();
    }

    public SeatResponse findSeatById(Long seatId) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public SeatResponse createSeat(SeatCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
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
