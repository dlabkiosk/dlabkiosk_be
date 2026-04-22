package com.moduletest.deasungkioskbackend.domain.seatleave.repository;

import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeave;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatLeaveRepository extends JpaRepository<SeatLeave, Long> {

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.id = :id")
    Optional<SeatLeave> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.student.id = :studentId "
        + "AND sl.startedAt >= :startOfDay AND sl.endedAt IS NULL")
    Optional<SeatLeave> findActiveSeatLeaveByStudentToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.store.id = :storeId "
        + "AND sl.startedAt >= :startOfDay "
        + "ORDER BY sl.startedAt DESC")
    List<SeatLeave> findAllByStoreIdToday(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.startedAt >= :startOfDay "
        + "ORDER BY sl.startedAt DESC")
    List<SeatLeave> findAllToday(
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "JOIN FETCH sl.store "
        + "WHERE sl.store.id = :storeId "
        + "AND sl.startedAt >= :startDate AND sl.startedAt < :endDate "
        + "ORDER BY sl.startedAt DESC")
    List<SeatLeave> findAllByStoreIdAndPeriod(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "JOIN FETCH sl.store "
        + "WHERE sl.startedAt >= :startDate AND sl.startedAt < :endDate "
        + "ORDER BY sl.startedAt DESC")
    List<SeatLeave> findAllByPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student s JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "JOIN FETCH sl.store "
        + "WHERE sl.startedAt >= :startDate AND sl.startedAt < :endDate "
        + "AND (:storeId IS NULL OR sl.store.id = :storeId) "
        + "AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%')) "
        + "AND (:studentNumber IS NULL "
        + "    OR s.studentNumber LIKE CONCAT('%', :studentNumber, '%'))",
        countQuery = "SELECT COUNT(sl) FROM SeatLeave sl JOIN sl.student s "
            + "WHERE sl.startedAt >= :startDate AND sl.startedAt < :endDate "
            + "AND (:storeId IS NULL OR sl.store.id = :storeId) "
            + "AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%')) "
            + "AND (:studentNumber IS NULL "
            + "    OR s.studentNumber LIKE CONCAT('%', :studentNumber, '%'))")
    Page<SeatLeave> findPageByFilter(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("studentName") String studentName,
        @Param("studentNumber") String studentNumber,
        Pageable pageable);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.student.id = :studentId "
        + "AND sl.startedAt >= :startDate AND sl.startedAt < :endDate "
        + "ORDER BY sl.startedAt DESC")
    List<SeatLeave> findAllByStudentIdAndPeriod(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.seat JOIN FETCH sl.reason "
        + "WHERE sl.store.id = :storeId "
        + "AND sl.startedAt >= :startOfDay "
        + "AND sl.endedAt IS NULL")
    List<SeatLeave> findActiveByStoreId(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT sl FROM SeatLeave sl "
        + "JOIN FETCH sl.student JOIN FETCH sl.seat JOIN FETCH sl.store "
        + "WHERE sl.startedAt >= :startOfDay "
        + "AND sl.endedAt IS NULL")
    List<SeatLeave> findAllActive(
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(sl) FROM SeatLeave sl "
        + "WHERE sl.store.id = :storeId "
        + "AND sl.startedAt >= :startOfDay")
    long countTodayByStoreId(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(sl) FROM SeatLeave sl "
        + "WHERE sl.store.id = :storeId "
        + "AND sl.startedAt >= :startOfDay "
        + "AND sl.endedAt IS NULL")
    long countActiveByStoreId(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);
}
