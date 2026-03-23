package com.moduletest.deasungkioskbackend.domain.phonesubmission.repository;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhoneSubmissionRepository extends JpaRepository<PhoneSubmission, Long> {


    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.store.id = :storeId "
        + "AND ps.submittedAt >= :startOfDay "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllByStoreIdToday(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.submittedAt >= :startOfDay "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllToday(
        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END "
        + "FROM PhoneSubmission ps "
        + "WHERE ps.student.id = :studentId "
        + "AND ps.startDate <= :endDate "
        + "AND (ps.endDate IS NULL OR ps.endDate >= :startDate)")
    boolean existsOverlapping(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END "
        + "FROM PhoneSubmission ps "
        + "WHERE ps.student.id = :studentId "
        + "AND ps.endDate IS NULL")
    boolean existsActiveNoPhone(@Param("studentId") Long studentId);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "WHERE ps.student.id = :studentId "
        + "AND (ps.endDate IS NULL OR ps.endDate >= :today)")
    List<PhoneSubmission> findActiveByStudentId(
        @Param("studentId") Long studentId,
        @Param("today") LocalDate today);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "WHERE ps.student.id = :studentId "
        + "AND ps.startDate <= :endDate "
        + "AND (ps.endDate IS NULL OR ps.endDate >= :startDate)")
    List<PhoneSubmission> findOverlapping(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.student.id = :studentId "
        + "AND ps.submittedAt >= :startDate AND ps.submittedAt < :endDate "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllByStudentIdAndPeriod(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.store.id = :storeId "
        + "AND ps.submittedAt >= :startDate AND ps.submittedAt < :endDate "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllByStoreIdAndPeriod(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.submittedAt >= :startDate AND ps.submittedAt < :endDate "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllByPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.store.id = :storeId "
        + "AND ps.submittedAt >= :startDate AND ps.submittedAt < :endDate",
        countQuery = "SELECT COUNT(ps) FROM PhoneSubmission ps "
            + "WHERE ps.store.id = :storeId "
            + "AND ps.submittedAt >= :startDate AND ps.submittedAt < :endDate")
    Page<PhoneSubmission> findPageByStoreIdAndPeriod(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    @Query(value = "SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.submittedAt >= :startDate AND ps.submittedAt < :endDate",
        countQuery = "SELECT COUNT(ps) FROM PhoneSubmission ps "
            + "WHERE ps.submittedAt >= :startDate AND ps.submittedAt < :endDate")
    Page<PhoneSubmission> findPageByPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
}
