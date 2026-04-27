package com.moduletest.deasungkioskbackend.domain.kiosk.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KioskAuthLogService {

    private static final int USER_AGENT_MAX_LENGTH = 500;

    private final KioskAuthLogRepository repository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(KioskAuthEventType eventType, Long storeId, String storeCode,
                    String reason, String ipAddress, String userAgent) {
        try {
            repository.save(KioskAuthLog.builder()
                .eventType(eventType)
                .storeId(storeId)
                .storeCode(truncate(storeCode, 50))
                .reason(reason)
                .ipAddress(ipAddress)
                .userAgent(truncate(userAgent, USER_AGENT_MAX_LENGTH))
                .build());
        } catch (Exception e) {
            log.error("키오스크 인증 로그 저장 실패. eventType: {}, storeId: {}, storeCode: {}",
                eventType, storeId, storeCode, e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
