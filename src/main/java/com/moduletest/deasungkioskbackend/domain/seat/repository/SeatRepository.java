package com.moduletest.deasungkioskbackend.domain.seat.repository;

import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s JOIN FETCH s.store WHERE s.store.id = :storeId")
    List<Seat> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT s FROM Seat s JOIN FETCH s.store")
    List<Seat> findAllWithStore();

    @Query("SELECT s FROM Seat s JOIN FETCH s.store WHERE s.id = :id")
    Optional<Seat> findByIdWithStore(@Param("id") Long id);

    Optional<Seat> findBySeatCdAndStoreId(String seatCd, Long storeId);
}
