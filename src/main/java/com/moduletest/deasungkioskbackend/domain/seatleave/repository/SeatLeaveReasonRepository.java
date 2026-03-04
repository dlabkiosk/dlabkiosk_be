package com.moduletest.deasungkioskbackend.domain.seatleave.repository;

import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeaveReason;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatLeaveReasonRepository extends JpaRepository<SeatLeaveReason, Long> {

    @Query("SELECT r FROM SeatLeaveReason r JOIN FETCH r.store WHERE r.id = :id")
    Optional<SeatLeaveReason> findByIdWithStore(@Param("id") Long id);

    @Query("SELECT r FROM SeatLeaveReason r JOIN FETCH r.store "
        + "WHERE r.store.id = :storeId ORDER BY r.displayOrder ASC")
    List<SeatLeaveReason> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT r FROM SeatLeaveReason r JOIN FETCH r.store "
        + "WHERE r.store.id = :storeId AND r.active = true ORDER BY r.displayOrder ASC")
    List<SeatLeaveReason> findAllActiveByStoreId(@Param("storeId") Long storeId);

}
