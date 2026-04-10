package com.moduletest.deasungkioskbackend.domain.meal.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.meal.dto.MealStatusResponse;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealTag;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealUnappliedLog;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealTagRepository;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealUnappliedLogRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.transaction.annotation.Propagation;
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
    private final MealUnappliedLogRepository mealUnappliedLogRepository;

    public List<MealStatusResponse> findMealStatusList(
            Long storeId, LocalDate startDate, LocalDate endDate,
            String studentName, String studentNumber) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        String nameFilter = (studentName != null && !studentName.isBlank())
            ? studentName.trim() : null;
        String numberFilter = (studentNumber != null && !studentNumber.isBlank())
            ? studentNumber.trim() : null;

        List<Student> students = studentRepository.findAllByStoreIdAndFilter(
            storeId, nameFilter, numberFilter);
        Map<String, Set<String>> dsaAppliedMap = fetchDsaMealApplied(store, startDate, endDate);
        List<MealTag> mealTags = mealTagRepository.findAllByStoreIdAndPeriod(
            storeId, startDate, endDate);
        Map<String, MealTag> mealTagMap = buildMealTagMap(mealTags);
        Map<String, MealUnappliedLog> unappliedMap = buildUnappliedMap(
            mealUnappliedLogRepository.findAllByStoreIdAndPeriod(storeId, startDate, endDate));

        List<MealStatusResponse> results = new ArrayList<>();

        for (Student student : students) {

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

                MealUnappliedLog lunchUnapplied = unappliedMap.get(
                    buildTagKey(student.getId(), date, MealType.LUNCH));
                MealUnappliedLog dinnerUnapplied = unappliedMap.get(
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
                    .lunchUnappliedDetected(lunchUnapplied != null)
                    .lunchUnappliedDetectedAt(lunchUnapplied != null
                        ? lunchUnapplied.getDetectedAt().toLocalTime() : null)
                    .dinnerApplied(dinnerApplied)
                    .dinnerChecked(dinnerTag != null)
                    .dinnerCheckedTime(dinnerTag != null
                        ? dinnerTag.getTaggedAt().toLocalTime() : null)
                    .dinnerUnappliedDetected(dinnerUnapplied != null)
                    .dinnerUnappliedDetectedAt(dinnerUnapplied != null
                        ? dinnerUnapplied.getDetectedAt().toLocalTime() : null)
                    .build());
            }
        }

        return results;
    }

    private Map<String, MealUnappliedLog> buildUnappliedMap(List<MealUnappliedLog> logs) {
        return logs.stream()
            .collect(Collectors.toMap(
                log -> buildTagKey(log.getStudent().getId(), log.getMealDate(), log.getMealType()),
                log -> log,
                (existing, duplicate) -> existing));
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
                        String stdNo = getStringValue(item, "std_no");
                        String day = getStringValue(item, "day");
                        String mealGb = getStringValue(item, "meal_gb");

                        if (stdNo == null || day == null || mealGb == null) {
                            log.warn("DSA 급식 신청 레코드 필드 누락 - month: {}, item: {}",
                                month, item);
                            continue;
                        }

                        LocalDate date;
                        try {
                            date = month.atDay(Integer.parseInt(day));
                        } catch (NumberFormatException | DateTimeException e) {
                            log.warn("DSA 급식 신청 day 파싱 실패 - month: {}, day: {}",
                                month, day);
                            continue;
                        }

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
                log.warn("DSA getMealApplyStdInfo 호출 실패 (month: {}). storeId: {} - {}",
                    month, store.getId(), e.getMessage());
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

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    /**
     * 관리자가 학생의 급식을 수동으로 체크 처리한다.
     * DSA 신청 여부 검증 후 MealTag row를 생성한다.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public MealStatusResponse markMealAsTagged(Long studentId, LocalDate date,
                                                MealType mealType) {
        Student student = studentRepository.findByIdWithStore(studentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        validateStoreAccess(student);

        if (mealTagRepository.existsByStudentIdAndMealDateAndMealType(
                studentId, date, mealType)) {
            throw new BusinessException(ErrorCode.ALREADY_MEAL_TAGGED);
        }

        // DSA 신청 검증
        Store store = student.getStore();
        Map<String, Set<String>> appliedMap = fetchDsaMealApplied(store, date, date);
        String mealLabel = mealType == MealType.LUNCH ? "점심" : "저녁";
        if (!isDsaMealApplied(appliedMap, student.getStudentNumber(), date, mealLabel)) {
            throw new BusinessException(ErrorCode.MEAL_NOT_APPLIED);
        }

        MealTag mealTag = MealTag.builder()
            .student(student)
            .store(store)
            .mealType(mealType)
            .mealDate(date)
            .taggedAt(LocalDateTime.now())
            .build();
        mealTagRepository.save(mealTag);

        log.info("관리자 수동 급식 체크 - studentId: {}, date: {}, mealType: {}",
            studentId, date, mealType);

        return buildSingleStatus(student, date, appliedMap);
    }

    /**
     * 관리자가 학생의 급식 체크를 해제(취소)한다.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public MealStatusResponse unmarkMealAsTagged(Long studentId, LocalDate date,
                                                  MealType mealType) {
        Student student = studentRepository.findByIdWithStore(studentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        validateStoreAccess(student);

        MealTag tag = mealTagRepository
            .findByStudentIdAndMealDateAndMealType(studentId, date, mealType)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_TAG_NOT_FOUND));

        mealTagRepository.delete(tag);

        log.info("관리자 수동 급식 체크 해제 - studentId: {}, date: {}, mealType: {}",
            studentId, date, mealType);

        Map<String, Set<String>> appliedMap = fetchDsaMealApplied(
            student.getStore(), date, date);
        return buildSingleStatus(student, date, appliedMap);
    }

    private void validateStoreAccess(Student student) {
        if (!SecurityUtil.isAdmin()) {
            Long currentStoreId = SecurityUtil.getCurrentStoreId();
            if (!student.getStore().getId().equals(currentStoreId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    private MealStatusResponse buildSingleStatus(Student student, LocalDate date,
                                                  Map<String, Set<String>> appliedMap) {
        String seatLabel = student.getAssignedSeat() != null
            ? student.getAssignedSeat().getSeatLabel() : null;

        boolean lunchApplied = isDsaMealApplied(
            appliedMap, student.getStudentNumber(), date, "점심");
        boolean dinnerApplied = isDsaMealApplied(
            appliedMap, student.getStudentNumber(), date, "저녁");

        MealTag lunchTag = mealTagRepository
            .findByStudentIdAndMealDateAndMealType(student.getId(), date, MealType.LUNCH)
            .orElse(null);
        MealTag dinnerTag = mealTagRepository
            .findByStudentIdAndMealDateAndMealType(student.getId(), date, MealType.DINNER)
            .orElse(null);

        MealUnappliedLog lunchUnapplied = mealUnappliedLogRepository
            .findByStudentIdAndMealDateAndMealType(student.getId(), date, MealType.LUNCH)
            .orElse(null);
        MealUnappliedLog dinnerUnapplied = mealUnappliedLogRepository
            .findByStudentIdAndMealDateAndMealType(student.getId(), date, MealType.DINNER)
            .orElse(null);

        return MealStatusResponse.builder()
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(seatLabel)
            .date(date)
            .lunchApplied(lunchApplied)
            .lunchChecked(lunchTag != null)
            .lunchCheckedTime(lunchTag != null
                ? lunchTag.getTaggedAt().toLocalTime() : null)
            .lunchUnappliedDetected(lunchUnapplied != null)
            .lunchUnappliedDetectedAt(lunchUnapplied != null
                ? lunchUnapplied.getDetectedAt().toLocalTime() : null)
            .dinnerApplied(dinnerApplied)
            .dinnerChecked(dinnerTag != null)
            .dinnerCheckedTime(dinnerTag != null
                ? dinnerTag.getTaggedAt().toLocalTime() : null)
            .dinnerUnappliedDetected(dinnerUnapplied != null)
            .dinnerUnappliedDetectedAt(dinnerUnapplied != null
                ? dinnerUnapplied.getDetectedAt().toLocalTime() : null)
            .build();
    }

}
