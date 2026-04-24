package com.moduletest.deasungkioskbackend.domain.attendance.repository;

import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a "
        + "JOIN FETCH a.student "
        + "JOIN FETCH a.store "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = :status")
    Optional<Attendance> findTodayAttendanceByStudentAndStatus(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay,
        @Param("status") AttendanceStatus status
    );

    @Query("SELECT COUNT(a) FROM Attendance a "
        + "WHERE a.store.id = :storeId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay")
    long countTodayByStoreId(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_OUT' "
        + "AND a.checkOutAction = 'C'")
    boolean existsEarlyLeaveToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(a) FROM Attendance a "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_OUT' "
        + "AND a.checkOutAction = 'C'")
    long countEarlyLeaveToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT a FROM Attendance a "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_OUT' "
        + "AND a.checkOutAction = 'C' "
        + "ORDER BY a.checkOutAt DESC")
    List<Attendance> findEarlyLeaveToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT a FROM Attendance a "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_OUT'")
    List<Attendance> findCompletedTodayByStudentId(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT a FROM Attendance a "
        + "WHERE a.store.id = :storeId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = :status")
    List<Attendance> findTodayByStoreIdAndStatus(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay,
        @Param("status") AttendanceStatus status
    );

    @Query("SELECT a FROM Attendance a "
        + "JOIN FETCH a.student "
        + "JOIN FETCH a.store "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_OUT' "
        + "ORDER BY a.checkOutAt DESC")
    List<Attendance> findLatestCheckedOutToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a "
        + "WHERE a.student.id = :studentId "
        + "AND a.checkInAt >= :startOfDay "
        + "AND a.checkInAt < :endOfDay "
        + "AND a.status = 'CHECKED_IN' "
        + "AND a.id <> :excludeAttendanceId")
    boolean existsOtherCheckedInToday(
        @Param("studentId") Long studentId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay,
        @Param("excludeAttendanceId") Long excludeAttendanceId
    );
}
