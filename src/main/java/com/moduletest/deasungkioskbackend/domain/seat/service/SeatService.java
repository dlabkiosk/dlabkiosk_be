package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
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
import java.util.HashMap;
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
    private final DsaAreaService dsaAreaService;


    @Transactional
    public List<SeatStatusResponse> findSeatStatusByArea(Long storeId, String areaCd) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<SeatStatusResponse> dsaSeats = dsaAreaService.findSeatStatusByArea(areaCd, store);

        // 우리 Redis에서 AWAY 상태인 좌석의 seatLabel 수집
        Map<Object, Object> redisStatus = seatRedisService.getSeatStatusMap(storeId);
        List<Seat> seats = seatRepository.findAllByStoreIdWithStore(storeId);

        Map<String, Seat> seatLabelMap = new HashMap<>();
        Map<String, Boolean> awaySeatLabels = new HashMap<>();
        for (Seat seat : seats) {
            seatLabelMap.put(seat.getSeatLabel(), seat);
            String status = (String) redisStatus.get(seat.getId().toString());
            if (status != null && status.startsWith("AWAY:")) {
                awaySeatLabels.put(seat.getSeatLabel(), true);
            }
        }

        // DSA 좌석 정보를 우리 DB 좌석에 동기화 (areaCd/areaNm은 건드리지 않음)
        for (SeatStatusResponse dsaSeat : dsaSeats) {
            Seat seat = seatLabelMap.get(dsaSeat.seatNm());
            if (seat != null) {
                seat.syncDsaSeatInfo(dsaSeat.seatCd(),
                    dsaSeat.xPos(), dsaSeat.yPos(), dsaSeat.seatGn());
            }
        }

        // DSA 좌석에 AWAY 상태 덮어씌우기
        return dsaSeats.stream()
            .map(s -> {
                boolean away = awaySeatLabels.containsKey(s.seatNm());
                if (away) {
                    return new SeatStatusResponse(
                        s.seatCd(), s.seatNm(), s.xPos(), s.yPos(),
                        s.seatGn(), "A", true);
                }
                return s;
            })
            .toList();
    }

    public List<AreaResponse> findAreasByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
        return dsaAreaService.findAreas(store);
    }

    // ===== 관리자 API =====

    public List<SeatResponse> findAllSeats(Long storeId, String areaCd) {
        List<Seat> seats;
        if (storeId != null) {
            seats = seatRepository.findAllByStoreIdWithStore(storeId);
        } else {
            seats = seatRepository.findAllWithStore();
        }

        return seats.stream()
            .filter(seat -> areaCd == null || areaCd.equals(seat.getAreaCd()))
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
