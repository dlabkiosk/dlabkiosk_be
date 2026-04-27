package com.moduletest.deasungkioskbackend.domain.kiosk.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaTokenService;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.security.TokenRedisService;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginRequest;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginResponse;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.kiosk.log.KioskAuthEventType;
import com.moduletest.deasungkioskbackend.domain.kiosk.log.KioskAuthLogService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KioskAuthService {

    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final DsaTokenService dsaTokenService;
    private final KioskAuthLogService kioskAuthLogService;

    public KioskLoginResult login(KioskLoginRequest request, String ipAddress, String userAgent) {
        Store store = storeRepository.findByStoreCode(request.storeCode())
            .orElseThrow(() -> {
                kioskAuthLogService.log(KioskAuthEventType.LOGIN_FAILED,
                    null, request.storeCode(),
                    ErrorCode.KIOSK_INVALID_STORE_CODE.getCode(), ipAddress, userAgent);
                return new KioskException(ErrorCode.KIOSK_INVALID_STORE_CODE);
            });

        if (!store.getKioskPin().equals(request.kioskPin())) {
            kioskAuthLogService.log(KioskAuthEventType.LOGIN_FAILED,
                store.getId(), request.storeCode(),
                ErrorCode.KIOSK_INVALID_PIN.getCode(), ipAddress, userAgent);
            throw new KioskException(ErrorCode.KIOSK_INVALID_PIN);
        }

        if (!store.isActive()) {
            kioskAuthLogService.log(KioskAuthEventType.LOGIN_FAILED,
                store.getId(), request.storeCode(),
                ErrorCode.KIOSK_STORE_INACTIVE.getCode(), ipAddress, userAgent);
            throw new KioskException(ErrorCode.KIOSK_STORE_INACTIVE);
        }

        String token = jwtTokenProvider.createKioskToken(store.getId(), store.getStoreCode());

        tokenRedisService.saveKioskToken(store.getId(), token);

        if (store.hasDsaCredentials()) {
            try {
                dsaTokenService.requestNewToken(store);
            } catch (Exception e) {
                log.warn("DSA 토큰 발급 실패 - 키오스크 로그인은 정상 처리. storeId: {}, error: {}",
                    store.getId(), e.getMessage());
            }
        }

        kioskAuthLogService.log(KioskAuthEventType.LOGIN_SUCCESS,
            store.getId(), store.getStoreCode(), null, ipAddress, userAgent);

        KioskLoginResponse loginResponse = KioskLoginResponse.fromEntity(store);
        return new KioskLoginResult(token, loginResponse);
    }

    public KioskLoginResponse findCurrentStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));
        return KioskLoginResponse.fromEntity(store);
    }

    public void logout(Long storeId, String token, String ipAddress, String userAgent) {
        tokenRedisService.removeKioskToken(storeId, token);
        String storeCode = storeRepository.findById(storeId)
            .map(Store::getStoreCode)
            .orElse(null);
        kioskAuthLogService.log(KioskAuthEventType.LOGOUT,
            storeId, storeCode, null, ipAddress, userAgent);
    }

    public record KioskLoginResult(String token, KioskLoginResponse storeInfo) { }
}
