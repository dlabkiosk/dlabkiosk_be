package com.moduletest.deasungkioskbackend.domain.meal.repository;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealTag;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealTagRepository extends JpaRepository<MealTag, Long> {

    boolean existsByStudentIdAndMealDateAndMealType(Long studentId, LocalDate mealDate,
                                                    MealType mealType);

    long countByStoreIdAndMealDateAndMealType(Long storeId, LocalDate mealDate,
                                               MealType mealType);

    List<MealTag> findAllByStudentIdAndMealDate(Long studentId, LocalDate mealDate);

    @Query("SELECT mt FROM MealTag mt "
        + "JOIN FETCH mt.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE mt.store.id = :storeId "
        + "AND mt.mealDate >= :startDate AND mt.mealDate <= :endDate "
        + "ORDER BY mt.mealDate, s.name")
    List<MealTag> findAllByStoreIdAndPeriod(
        @Param("storeId") Long storeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
