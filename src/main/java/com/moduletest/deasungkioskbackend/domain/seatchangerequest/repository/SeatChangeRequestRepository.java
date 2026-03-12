package com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository;

import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatChangeRequestRepository extends JpaRepository<SeatChangeRequest, Long> {

    @Query("SELECT r FROM SeatChangeRequest r"
        + " JOIN FETCH r.student"
        + " JOIN FETCH r.store"
        + " LEFT JOIN FETCH r.currentSeat"
        + " JOIN FETCH r.desiredSeat1"
        + " LEFT JOIN FETCH r.desiredSeat2"
        + " LEFT JOIN FETCH r.desiredSeat3"
        + " LEFT JOIN FETCH r.approvedSeat"
        + " WHERE r.id = :id")
    Optional<SeatChangeRequest> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT r FROM SeatChangeRequest r"
        + " JOIN FETCH r.student"
        + " JOIN FETCH r.store"
        + " LEFT JOIN FETCH r.currentSeat"
        + " JOIN FETCH r.desiredSeat1"
        + " LEFT JOIN FETCH r.desiredSeat2"
        + " LEFT JOIN FETCH r.desiredSeat3"
        + " LEFT JOIN FETCH r.approvedSeat"
        + " WHERE r.store.id = :storeId AND r.status = :status",
        countQuery = "SELECT COUNT(r) FROM SeatChangeRequest r"
            + " WHERE r.store.id = :storeId AND r.status = :status")
    Page<SeatChangeRequest> findAllByStoreIdAndStatusWithDetails(
        @Param("storeId") Long storeId,
        @Param("status") SeatChangeRequestStatus status,
        Pageable pageable);

    @Query(value = "SELECT r FROM SeatChangeRequest r"
        + " JOIN FETCH r.student"
        + " JOIN FETCH r.store"
        + " LEFT JOIN FETCH r.currentSeat"
        + " JOIN FETCH r.desiredSeat1"
        + " LEFT JOIN FETCH r.desiredSeat2"
        + " LEFT JOIN FETCH r.desiredSeat3"
        + " LEFT JOIN FETCH r.approvedSeat"
        + " WHERE r.status = :status",
        countQuery = "SELECT COUNT(r) FROM SeatChangeRequest r"
            + " WHERE r.status = :status")
    Page<SeatChangeRequest> findAllByStatusWithDetails(
        @Param("status") SeatChangeRequestStatus status,
        Pageable pageable);

    boolean existsByStudentIdAndStatus(Long studentId, SeatChangeRequestStatus status);

    @Query("SELECT r FROM SeatChangeRequest r"
        + " LEFT JOIN FETCH r.currentSeat"
        + " JOIN FETCH r.desiredSeat1"
        + " LEFT JOIN FETCH r.desiredSeat2"
        + " LEFT JOIN FETCH r.desiredSeat3"
        + " LEFT JOIN FETCH r.approvedSeat"
        + " WHERE r.student.id = :studentId"
        + " ORDER BY r.createdAt DESC")
    List<SeatChangeRequest> findAllByStudentIdWithDetails(
        @Param("studentId") Long studentId);

    @Query("SELECT r FROM SeatChangeRequest r"
        + " WHERE (r.desiredSeat1.id = :seatId"
        + " OR r.desiredSeat2.id = :seatId"
        + " OR r.desiredSeat3.id = :seatId)"
        + " AND r.status = :status")
    List<SeatChangeRequest> findAllByDesiredSeatIdAndStatus(
        @Param("seatId") Long seatId,
        @Param("status") SeatChangeRequestStatus status);

    @Query("SELECT r FROM SeatChangeRequest r"
        + " JOIN FETCH r.student"
        + " JOIN FETCH r.desiredSeat1"
        + " LEFT JOIN FETCH r.desiredSeat2"
        + " LEFT JOIN FETCH r.desiredSeat3"
        + " WHERE r.store.id = :storeId"
        + " AND r.status = :status"
        + " ORDER BY r.createdAt ASC")
    List<SeatChangeRequest> findAllByStoreIdAndStatusWithStudent(
        @Param("storeId") Long storeId,
        @Param("status") SeatChangeRequestStatus status);

    @Query("SELECT r FROM SeatChangeRequest r"
        + " JOIN FETCH r.student"
        + " JOIN FETCH r.desiredSeat1"
        + " WHERE r.store.id = :storeId"
        + " AND r.status = :status"
        + " ORDER BY r.createdAt DESC")
    List<SeatChangeRequest> findRecentByStoreIdAndStatus(
        @Param("storeId") Long storeId,
        @Param("status") SeatChangeRequestStatus status,
        Pageable pageable);
}
