package com.moduletest.deasungkioskbackend.domain.outing.repository;

import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutingRepository extends JpaRepository<Outing, Long> {

    @Query("SELECT o FROM Outing o "
        + "JOIN FETCH o.student "
        + "WHERE o.student.id = :studentId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NULL")
    Optional<Outing> findActiveOutingByStudentToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT o FROM Outing o "
        + "JOIN FETCH o.student "
        + "WHERE o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NULL")
    List<Outing> findAllActiveOutingsToday(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(o) FROM Outing o "
        + "WHERE o.store.id = :storeId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NULL")
    long countActiveByStoreIdToday(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Outing o "
        + "WHERE o.student.id = :studentId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NOT NULL")
    boolean existsCompletedOutingToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(o) FROM Outing o "
        + "WHERE o.student.id = :studentId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NOT NULL")
    long countCompletedOutingToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT o FROM Outing o "
        + "JOIN FETCH o.student s "
        + "JOIN FETCH s.store "
        + "JOIN FETCH o.store "
        + "WHERE o.id = :id")
    Optional<Outing> findByIdWithStudentAndStore(@Param("id") Long id);

    @Query("SELECT o FROM Outing o "
        + "JOIN FETCH o.student s "
        + "JOIN FETCH s.store "
        + "JOIN FETCH o.store "
        + "WHERE o.student.id = :studentId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "ORDER BY o.startedAt DESC")
    List<Outing> findLatestTodayByStudentId(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT o FROM Outing o "
        + "JOIN FETCH o.student s "
        + "JOIN FETCH s.store "
        + "JOIN FETCH o.store "
        + "WHERE o.student.id = :studentId "
        + "AND o.startedAt >= :startOfDay "
        + "AND o.startedAt < :endOfDay "
        + "AND o.endedAt IS NULL")
    Optional<Outing> findActiveTodayByStudentIdWithStore(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
}
