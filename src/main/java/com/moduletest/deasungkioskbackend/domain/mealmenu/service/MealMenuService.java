package com.moduletest.deasungkioskbackend.domain.mealmenu.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuDayRequest;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuWeekRequest;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuWeekResponse;
import com.moduletest.deasungkioskbackend.domain.mealmenu.entity.MealMenu;
import com.moduletest.deasungkioskbackend.domain.mealmenu.repository.MealMenuRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealMenuService {

    private final MealMenuRepository mealMenuRepository;
    private final StoreRepository storeRepository;

    public MealMenuWeekResponse findWeeklyMenu(Long storeId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<MealMenu> menus = mealMenuRepository
            .findAllByStoreIdAndWeek(storeId, weekStartDate, weekEndDate);
        return MealMenuWeekResponse.of(weekStartDate, menus);
    }

    @Transactional
    public MealMenuWeekResponse saveWeeklyMenu(Long storeId, MealMenuWeekRequest request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        for (MealMenuDayRequest day : request.days()) {
            Optional<MealMenu> existing = mealMenuRepository
                .findByStoreIdAndMenuDate(storeId, day.menuDate());

            if (existing.isPresent()) {
                existing.get().updateMenu(day.lunch(), day.dinner(), day.closed());
            } else {
                MealMenu mealMenu = MealMenu.builder()
                    .store(store)
                    .menuDate(day.menuDate())
                    .lunch(day.lunch())
                    .dinner(day.dinner())
                    .closed(day.closed())
                    .build();
                mealMenuRepository.save(mealMenu);
            }
        }

        LocalDate weekEndDate = request.weekStartDate().plusDays(6);
        List<MealMenu> menus = mealMenuRepository
            .findAllByStoreIdAndWeek(storeId, request.weekStartDate(), weekEndDate);
        return MealMenuWeekResponse.of(request.weekStartDate(), menus);
    }

    @Transactional
    public void deleteWeeklyMenu(Long storeId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<MealMenu> menus = mealMenuRepository
            .findAllByStoreIdAndWeek(storeId, weekStartDate, weekEndDate);
        mealMenuRepository.deleteAll(menus);
    }
}
