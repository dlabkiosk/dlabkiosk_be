package com.moduletest.deasungkioskbackend.domain.meal.repository;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealTag;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealTagRepository extends JpaRepository<MealTag, Long> {

    boolean existsByStudentIdAndMealDateAndMealType(Long studentId, LocalDate mealDate,
                                                    MealType mealType);

    long countByStoreIdAndMealDateAndMealType(Long storeId, LocalDate mealDate,
                                               MealType mealType);

    List<MealTag> findAllByStudentIdAndMealDate(Long studentId, LocalDate mealDate);
}
