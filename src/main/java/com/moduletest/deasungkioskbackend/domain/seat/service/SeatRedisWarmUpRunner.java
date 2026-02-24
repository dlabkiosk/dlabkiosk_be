package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final OutingRepository outingRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) throws Exception {

        try {
            List<SeatUsage> activeUsages = seatUsageRepository
                .findAllByStatusWithDetails(SeatUsageStatus.IN_USE);

            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            Set<Long> outingStudentIds = outingRepository
                .findAllActiveOutingsToday(startOfDay, endOfDay)
                .stream()
                .map(outing -> outing.getStudent().getId())
                .collect(Collectors.toSet());

            int outingCount = 0;
            for (SeatUsage usage : activeUsages) {
                Long studentId = usage.getStudent().getId();
                String studentName = usage.getStudent().getName();

                String value;
                if (outingStudentIds.contains(studentId)) {
                    value = "OUTING:" + studentId + ":" + studentName;
                    outingCount++;
                } else {
                    value = "IN_USE:" + studentId + ":" + studentName;
                }

                seatRedisService.warmUpSeat(
                    usage.getStore().getId(), usage.getSeat().getId(), value);
            }

            log.info("[SeatRedisWarmUp] Redis 좌석 상태 초기화 완료 (사용 중 {}석, 외출 중 {}석)",
                activeUsages.size() - outingCount, outingCount);
        } catch (Exception e) {
            log.warn("[SeatRedisWarmUp] Redis 초기화 실패 — 서버는 정상 기동됩니다", e);
        }

    }
}
