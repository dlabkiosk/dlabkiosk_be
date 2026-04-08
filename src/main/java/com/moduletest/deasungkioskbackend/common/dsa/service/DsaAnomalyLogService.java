package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moduletest.deasungkioskbackend.common.dsa.entity.DsaAnomalyLog;
import com.moduletest.deasungkioskbackend.common.dsa.repository.DsaAnomalyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DsaAnomalyLogService {

    private final DsaAnomalyLogRepository repository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long storeId, String rfidUid, String apiPath, String anomalyType,
                    Object requestParams, Object rawResponse, String note) {
        try {
            repository.save(DsaAnomalyLog.builder()
                .storeId(storeId)
                .rfidUid(rfidUid)
                .apiPath(apiPath)
                .anomalyType(anomalyType)
                .requestParams(toJson(requestParams))
                .rawResponse(toJson(rawResponse))
                .note(note)
                .build());
        } catch (Exception e) {
            log.error("DSA 이상 응답 로그 저장 실패. type: {}, storeId: {}",
                anomalyType, storeId, e);
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
