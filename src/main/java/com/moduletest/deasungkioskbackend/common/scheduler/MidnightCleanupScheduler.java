package com.moduletest.deasungkioskbackend.common.scheduler;

import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자정 직후 어제 마감 안 된 데이터 정리.
 * - Attendance CHECKED_IN (하원 안 한 거) → CHECKED_OUT으로 닫음
 * - SeatUsage IN_USE (좌석 점유 중) → ENDED로 닫음 (어제 IN_USE 남으면 오늘 등원 시 좌석 점유 차단됨)
 * - Outing 진행중, SeatLeave 진행중 → 종료
 * - Redis 좌석 Hash 전체 청소 (오늘 등원하면서 다시 채워짐)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class MidnightCleanupScheduler {

    private static final String SEAT_STATUS_KEY_PATTERN = "seat:status:*";

    private final AttendanceRepository attendanceRepository;
    private final OutingRepository outingRepository;
    private final SeatLeaveRepository seatLeaveRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "${cleanup.midnight.cron:0 30 0 * * *}")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        log.info("[자정정리] 시작 — startOfToday={}, closedAt={}", startOfToday, now);

        int closedAttendances = attendanceRepository
            .closeDanglingCheckIns(startOfToday, now);
        int closedOutings = outingRepository
            .closeDanglingOutings(startOfToday, now);
        int closedSeatLeaves = seatLeaveRepository
            .closeDanglingSeatLeaves(startOfToday, now);
        int closedSeatUsages = seatUsageRepository
            .closeDanglingUsages(startOfToday, now);

        long deletedRedisKeys = clearSeatStatusRedis();

        log.info("[자정정리] 완료 — Attendance: {}건, Outing: {}건, "
                + "SeatLeave: {}건, SeatUsage: {}건, Redis 키: {}건",
            closedAttendances, closedOutings, closedSeatLeaves,
            closedSeatUsages, deletedRedisKeys);
    }

    private long clearSeatStatusRedis() {
        try {
            Set<String> keys = redisTemplate.keys(SEAT_STATUS_KEY_PATTERN);
            if (keys == null || keys.isEmpty()) {
                return 0;
            }
            Long deleted = redisTemplate.delete(keys);
            return deleted == null ? 0 : deleted;
        } catch (Exception e) {
            log.error("[자정정리] Redis 좌석 Hash 청소 실패", e);
            return 0;
        }
    }
}
