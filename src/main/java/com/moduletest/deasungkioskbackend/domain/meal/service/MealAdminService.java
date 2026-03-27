package com.moduletest.deasungkioskbackend.domain.meal.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.meal.dto.MealStatusResponse;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealTag;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealTagRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealAdminService {

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;
    private final MealTagRepository mealTagRepository;

    public List<MealStatusResponse> findMealStatusList(
            Long storeId, LocalDate startDate, LocalDate endDate,
            String studentName, String studentNumber) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, Set<String>> dsaAppliedMap = fetchDsaMealApplied(store, startDate, endDate);
        List<MealTag> mealTags = mealTagRepository.findAllByStoreIdAndPeriod(
            storeId, startDate, endDate);
        Map<String, MealTag> mealTagMap = buildMealTagMap(mealTags);

        List<MealStatusResponse> results = new ArrayList<>();

        for (Student student : students) {
            if (!matchesFilter(student, studentName, studentNumber)) {
                continue;
            }

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                String seatLabel = student.getAssignedSeat() != null
                    ? student.getAssignedSeat().getSeatLabel() : null;

                boolean lunchApplied = isDsaMealApplied(
                    dsaAppliedMap, student.getStudentNumber(), date, "점심");
                boolean dinnerApplied = isDsaMealApplied(
                    dsaAppliedMap, student.getStudentNumber(), date, "저녁");

                MealTag lunchTag = mealTagMap.get(
                    buildTagKey(student.getId(), date, MealType.LUNCH));
                MealTag dinnerTag = mealTagMap.get(
                    buildTagKey(student.getId(), date, MealType.DINNER));

                results.add(MealStatusResponse.builder()
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .seatLabel(seatLabel)
                    .date(date)
                    .lunchApplied(lunchApplied)
                    .lunchChecked(lunchTag != null)
                    .lunchCheckedTime(lunchTag != null
                        ? lunchTag.getTaggedAt().toLocalTime() : null)
                    .dinnerApplied(dinnerApplied)
                    .dinnerChecked(dinnerTag != null)
                    .dinnerCheckedTime(dinnerTag != null
                        ? dinnerTag.getTaggedAt().toLocalTime() : null)
                    .build());
            }
        }

        return results;
    }

    private Map<String, Set<String>> fetchDsaMealApplied(
            Store store, LocalDate startDate, LocalDate endDate) {
        Map<String, Set<String>> appliedMap = new HashMap<>();

        if (!store.hasDsaCredentials()) {
            return appliedMap;
        }

        Set<YearMonth> months = new HashSet<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            months.add(YearMonth.from(d));
        }

        for (YearMonth month : months) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("month", month.toString());

                DsaResponse response = dsaApiClient.post(
                    "/kiosk/getMealApplyStdInfo", params, DsaResponse.class, store);

                if (response.isSuccess() && response.getDataAsList() != null) {
                    for (Map<String, Object> item : response.getDataAsList()) {
                        String stdNo = String.valueOf(item.get("std_no"));
                        String day = String.valueOf(item.get("day"));
                        String mealGb = String.valueOf(item.get("meal_gb"));
                        LocalDate date = month.atDay(Integer.parseInt(day.trim()));

                        String key = stdNo + ":" + date;
                        appliedMap.computeIfAbsent(key, k -> new HashSet<>());

                        if (mealGb.contains("점심")) {
                            appliedMap.get(key).add("점심");
                        }
                        if (mealGb.contains("저녁")) {
                            appliedMap.get(key).add("저녁");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("DSA getMealApplyStdInfo 호출 실패 (month: {}): {}",
                    month, e.getMessage());
            }
        }

        return appliedMap;
    }

    private boolean isDsaMealApplied(Map<String, Set<String>> appliedMap,
                                      String studentNumber, LocalDate date,
                                      String mealLabel) {
        if (studentNumber == null) {
            return false;
        }
        String key = studentNumber + ":" + date;
        Set<String> meals = appliedMap.get(key);
        return meals != null && meals.contains(mealLabel);
    }

    private Map<String, MealTag> buildMealTagMap(List<MealTag> mealTags) {
        return mealTags.stream()
            .collect(Collectors.toMap(
                mt -> buildTagKey(mt.getStudent().getId(), mt.getMealDate(), mt.getMealType()),
                mt -> mt,
                (existing, duplicate) -> existing));
    }

    private String buildTagKey(Long studentId, LocalDate date, MealType mealType) {
        return studentId + ":" + date + ":" + mealType;
    }

    private boolean matchesFilter(Student student, String studentName, String studentNumber) {
        if (studentName != null && !studentName.isBlank()
            && !student.getName().contains(studentName)) {
            return false;
        }
        if (studentNumber != null && !studentNumber.isBlank()
            && !studentNumber.equals(student.getStudentNumber())) {
            return false;
        }
        return true;
    }
}
