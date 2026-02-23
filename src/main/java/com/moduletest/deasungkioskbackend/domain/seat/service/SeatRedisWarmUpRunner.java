package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class SeatRedisWarmUpRunner implements ApplicationRunner {

    private final SeatRedisService seatRedisService;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) throws Exception {

        try {
            List<Seat> allSeats = seatRepository.findAll();
            for (Seat seat : allSeats) {
                SeatUsage activeUsage = seatUsageRepository
                    .findBySeatIdAndStatus(seat.getId(), SeatUsageStatus.IN_USE)
                    .orElse(null);

                if (activeUsage != null) {
                    String value = "IN_USE:"
                        + activeUsage.getStudent().getId() + ":" + activeUsage.getStudent().getName();

                    seatRedisService.warmUpSeat(
                        seat.getStore().getId(), seat.getId(), value);
                }
            }

            log.info("[SeatRedisWarmUp] Redis 좌석 상태 초기화 완료 (총 {}석)", allSeats.size());
        } catch (Exception e) {
            log.warn("[SeatRedisWarmUp] Redis 초기화 실패 — 서버는 정상 기동됩니다", e);
        }

    }
}
