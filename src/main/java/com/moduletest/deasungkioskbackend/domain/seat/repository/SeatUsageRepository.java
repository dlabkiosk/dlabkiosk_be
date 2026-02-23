package com.moduletest.deasungkioskbackend.domain.seat.repository;

import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatUsageRepository extends JpaRepository<SeatUsage, Long> {
    
    Optional<SeatUsage> findByStudentIdAndStatus(Long studentId, SeatUsageStatus status);

    Optional<SeatUsage> findBySeatIdAndStatus(Long seat_id, SeatUsageStatus status);

}
