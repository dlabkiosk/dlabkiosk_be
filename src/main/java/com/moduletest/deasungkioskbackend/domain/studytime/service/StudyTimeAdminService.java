package com.moduletest.deasungkioskbackend.domain.studytime.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.studytime.dto.StudyTimeStudentResponse;
import com.moduletest.deasungkioskbackend.domain.studytime.dto.StudyTimeStudentResponse.DailyStudyTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public final class StudyTimeAdminService {

    private static final Pattern TIME_PATTERN =
        Pattern.compile("(\\d+)시간\\s*(\\d+)분");
    private static final String ZERO_TIME = "0시간 0분";

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;

    public List<StudyTimeStudentResponse> findStudyTimeList(
            Long storeId, LocalDate startDate, LocalDate endDate,
            String studentName, String studentNumber) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, Map<LocalDate, DsaStudyRecord>> dsaMap =
            fetchAndGroupDsaStudyTime(store, startDate, endDate);

        List<StudyTimeStudentResponse> results = new ArrayList<>();

        for (Student student : students) {
            if (!matchesFilter(student, studentName, studentNumber)) {
                continue;
            }

            String seatLabel = student.getAssignedSeat() != null
                ? student.getAssignedSeat().getSeatLabel() : null;

            Map<LocalDate, DsaStudyRecord> studentDsa =
                dsaMap.getOrDefault(student.getStudentNumber(), Map.of());

            List<DailyStudyTime> dailyList = new ArrayList<>();
            int totalMinutes = 0;

            for (LocalDate date = startDate; !date.isAfter(endDate);
                 date = date.plusDays(1)) {
                DsaStudyRecord record = studentDsa.get(date);
                int minutes = record != null ? record.minutes : 0;
                String timeStr = record != null ? record.timeStr : ZERO_TIME;

                dailyList.add(DailyStudyTime.builder()
                    .date(date)
                    .studyTimeMinutes(minutes)
                    .studyTime(timeStr)
                    .build());
                totalMinutes += minutes;
            }

            results.add(StudyTimeStudentResponse.builder()
                .studentName(student.getName())
                .studentNumber(student.getStudentNumber())
                .seatLabel(seatLabel)
                .totalMinutes(totalMinutes)
                .totalStudyTime(formatMinutes(totalMinutes))
                .dailyStudyTimes(dailyList)
                .build());
        }

        return results;
    }

    private Map<String, Map<LocalDate, DsaStudyRecord>> fetchAndGroupDsaStudyTime(
            Store store, LocalDate startDate, LocalDate endDate) {
        Map<String, Map<LocalDate, DsaStudyRecord>> result = new HashMap<>();

        if (!store.hasDsaCredentials()) {
            return result;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("st_dt", startDate.toString());
            params.put("ed_dt", endDate.toString());

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getStudyTimeList", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getData() == null) {
                log.warn("DSA getStudyTimeList 실패 - code: {}, message: {}",
                    response.getCode(), response.getMessage());
                return result;
            }

            for (Map<String, Object> item : response.getData()) {
                String stdNo = getStringValue(item, "std_no");
                String studyDt = getStringValue(item, "study_dt");
                String attTm = getStringValue(item, "att_tm");

                if (stdNo == null || studyDt == null) {
                    continue;
                }

                LocalDate date = LocalDate.parse(studyDt);
                int minutes = parseStudyTimeMinutes(attTm);
                String timeStr = attTm != null ? attTm : ZERO_TIME;

                result.computeIfAbsent(stdNo, k -> new HashMap<>())
                    .put(date, new DsaStudyRecord(minutes, timeStr));
            }
        } catch (Exception e) {
            log.warn("DSA getStudyTimeList 호출 실패: {}", e.getMessage());
        }

        return result;
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

    private int parseStudyTimeMinutes(String studyTime) {
        if (studyTime == null || studyTime.isBlank()) {
            return 0;
        }
        Matcher matcher = TIME_PATTERN.matcher(studyTime);
        if (matcher.find()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            return hours * 60 + minutes;
        }
        return 0;
    }

    private String formatMinutes(int totalMinutes) {
        return totalMinutes / 60 + "시간 " + totalMinutes % 60 + "분";
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    private record DsaStudyRecord(int minutes, String timeStr) {
    }
}
