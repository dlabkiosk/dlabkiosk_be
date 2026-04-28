package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatType;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좌석 동기화의 트랜잭션 단위 작업.
 * 구역(area) 단위로 짧은 별도 트랜잭션으로 좌석 upsert를 수행한다.
 * SeatService.synchronizeSeats가 외부에서 호출해야 Spring AOP 프록시가 적용된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatSyncTxService {

    private final SeatRepository seatRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void upsertSeatsForArea(Long storeId, String areaCd, String areaNm,
        List<SeatStatusResponse> dsaSeats) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        for (SeatStatusResponse dsaSeat : dsaSeats) {
            if (dsaSeat.seatNm() == null || dsaSeat.seatNm().isEmpty()) {
                continue;
            }
            // Why: seatCd로 store 전체에서 조회. 기존 좌석이 다른 area에 있다가
            // 옮겨온 경우에도 같은 row를 update해야 하므로 area 한정 조회 X.
            Optional<Seat> existing = seatRepository.findBySeatCdAndStoreId(
                dsaSeat.seatCd(), storeId);
            if (existing.isPresent()) {
                existing.get().syncFromDsa(dsaSeat.seatNm(), dsaSeat.seatCd(),
                    dsaSeat.xPos(), dsaSeat.yPos(),
                    areaCd, areaNm, dsaSeat.seatGn());
            } else {
                seatRepository.save(Seat.builder()
                    .store(store)
                    .seatLabel(dsaSeat.seatNm())
                    .seatType(SeatType.INDIVIDUAL)
                    .xPos(dsaSeat.xPos())
                    .yPos(dsaSeat.yPos())
                    .active(true)
                    .areaCd(areaCd)
                    .areaNm(areaNm)
                    .seatCd(dsaSeat.seatCd())
                    .seatGn(dsaSeat.seatGn())
                    .dsaSynced(true)
                    .build());
            }
        }
    }
}
