package com.moduletest.deasungkioskbackend.domain.meal.repository;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealUnappliedLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealUnappliedLogRepository extends JpaRepository<MealUnappliedLog, Long> {

    boolean existsByStudentIdAndMealDateAndMealType(
        Long studentId, LocalDate mealDate, MealType mealType);

    Optional<MealUnappliedLog> findByStudentIdAndMealDateAndMealType(
        Long studentId, LocalDate mealDate, MealType mealType);

    @Query("SELECT log FROM MealUnappliedLog log"
        + " WHERE log.store.id = :storeId"
        + " AND log.mealDate BETWEEN :startDate AND :endDate")
    List<MealUnappliedLog> findAllByStoreIdAndPeriod(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
