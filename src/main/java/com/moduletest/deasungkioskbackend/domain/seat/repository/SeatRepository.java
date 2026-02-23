package com.moduletest.deasungkioskbackend.domain.seat.repository;

import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByStoreId(Long storeId);

}
