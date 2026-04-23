package com.moduletest.deasungkioskbackend.domain.seat.repository;

import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatUsageRepository extends JpaRepository<SeatUsage, Long> {

    Optional<SeatUsage> findByStudentIdAndStatus(Long studentId, SeatUsageStatus status);

    Optional<SeatUsage> findBySeatIdAndStatus(Long seatId, SeatUsageStatus status);

    @Query("SELECT su FROM SeatUsage su "
        + "JOIN FETCH su.seat "
        + "JOIN FETCH su.student "
        + "JOIN FETCH su.store "
        + "WHERE su.status = :status")
    List<SeatUsage> findAllByStatusWithDetails(@Param("status") SeatUsageStatus status);

    @Query("SELECT su FROM SeatUsage su "
        + "JOIN FETCH su.seat "
        + "WHERE su.student.id = :studentId "
        + "AND su.status = 'ENDED' "
        + "AND su.startedAt >= :startOfDay "
        + "AND su.startedAt < :endOfDay "
        + "ORDER BY su.endedAt DESC")
    List<SeatUsage> findEndedTodayByStudentId(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay);

}
