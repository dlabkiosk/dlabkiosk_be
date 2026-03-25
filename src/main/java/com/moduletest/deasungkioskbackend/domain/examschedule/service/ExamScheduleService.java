package com.moduletest.deasungkioskbackend.domain.examschedule.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleCreateRequest;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleResponse;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.examschedule.entity.ExamSchedule;
import com.moduletest.deasungkioskbackend.domain.examschedule.exception.ExamScheduleException;
import com.moduletest.deasungkioskbackend.domain.examschedule.repository.ExamScheduleRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final StoreRepository storeRepository;

    public List<ExamScheduleResponse> findAllExamSchedules() {
        return examScheduleRepository.findAllWithStore()
            .stream()
            .map(ExamScheduleResponse::fromEntity)
            .toList();
    }

    public List<ExamScheduleResponse> findAllExamSchedulesByStoreId(Long storeId) {
        return examScheduleRepository.findAllByStoreIdWithStore(storeId)
            .stream()
            .map(ExamScheduleResponse::fromEntity)
            .toList();
    }

    public List<ExamScheduleResponse> findActiveExamSchedulesByStoreId(Long storeId) {
        return examScheduleRepository.findAllActiveByStoreIdWithStore(storeId)
            .stream()
            .map(ExamScheduleResponse::fromEntity)
            .toList();
    }

    public ExamScheduleResponse findExamScheduleById(Long examScheduleId) {
        ExamSchedule examSchedule = examScheduleRepository.findByIdWithStore(examScheduleId)
            .orElseThrow(() -> new ExamScheduleException(ErrorCode.EXAM_SCHEDULE_NOT_FOUND));
        return ExamScheduleResponse.fromEntity(examSchedule);
    }

    @Transactional
    public ExamScheduleResponse createExamSchedule(ExamScheduleCreateRequest request,
        Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        ExamSchedule examSchedule = ExamSchedule.builder()
            .store(store)
            .examName(request.examName())
            .examDate(request.examDate())
            .build();

        ExamSchedule savedExamSchedule = examScheduleRepository.save(examSchedule);
        return ExamScheduleResponse.fromEntity(savedExamSchedule);
    }

    @Transactional
    public ExamScheduleResponse updateExamSchedule(Long examScheduleId,
        ExamScheduleUpdateRequest request) {
        ExamSchedule examSchedule = examScheduleRepository.findByIdWithStore(examScheduleId)
            .orElseThrow(() -> new ExamScheduleException(ErrorCode.EXAM_SCHEDULE_NOT_FOUND));

        examSchedule.updateInfo(request.examName(), request.examDate());
        return ExamScheduleResponse.fromEntity(examSchedule);
    }

    @Transactional
    public void deleteExamSchedule(Long examScheduleId) {
        ExamSchedule examSchedule = examScheduleRepository.findById(examScheduleId)
            .orElseThrow(() -> new ExamScheduleException(ErrorCode.EXAM_SCHEDULE_NOT_FOUND));
        examScheduleRepository.delete(examSchedule);
    }

    @Transactional
    public ExamScheduleResponse toggleActive(Long examScheduleId) {
        ExamSchedule examSchedule = examScheduleRepository.findByIdWithStore(examScheduleId)
            .orElseThrow(() -> new ExamScheduleException(ErrorCode.EXAM_SCHEDULE_NOT_FOUND));
        examSchedule.toggleActive();
        return ExamScheduleResponse.fromEntity(examSchedule);
    }
}
