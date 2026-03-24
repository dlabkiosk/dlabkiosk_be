package com.moduletest.deasungkioskbackend.domain.mealmenu.repository;

import com.moduletest.deasungkioskbackend.domain.mealmenu.entity.MealMenu;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealMenuRepository extends JpaRepository<MealMenu, Long> {

    @Query("SELECT m FROM MealMenu m "
        + "JOIN FETCH m.store "
        + "WHERE m.store.id = :storeId "
        + "AND m.menuDate BETWEEN :startDate AND :endDate "
        + "ORDER BY m.menuDate ASC")
    List<MealMenu> findAllByStoreIdAndWeek(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    Optional<MealMenu> findByStoreIdAndMenuDate(Long storeId, LocalDate menuDate);
}
