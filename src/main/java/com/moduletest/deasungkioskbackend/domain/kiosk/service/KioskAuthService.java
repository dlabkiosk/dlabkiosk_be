package com.moduletest.deasungkioskbackend.domain.kiosk.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaTokenService;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.security.TokenRedisService;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginRequest;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginResponse;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
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

    public KioskLoginResult login(KioskLoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.storeCode())
            .orElseThrow(() -> new KioskException(ErrorCode.KIOSK_INVALID_CREDENTIALS));

        if (!store.getKioskPin().equals(request.kioskPin())) {
            throw new KioskException(ErrorCode.KIOSK_INVALID_CREDENTIALS);
        }

        if (!store.isActive()) {
            throw new KioskException(ErrorCode.KIOSK_STORE_INACTIVE);
        }

        String token = jwtTokenProvider.createKioskToken(store.getId(), store.getStoreCode());

        tokenRedisService.saveKioskToken(
            store.getId(), token, jwtTokenProvider.getKioskExpiration());

        if (store.hasDsaCredentials()) {
            try {
                dsaTokenService.requestNewToken(store);
            } catch (Exception e) {
                log.warn("DSA 토큰 발급 실패 - 키오스크 로그인은 정상 처리. storeId: {}, error: {}",
                    store.getId(), e.getMessage());
            }
        }

        KioskLoginResponse loginResponse = KioskLoginResponse.fromEntity(store);
        return new KioskLoginResult(token, loginResponse);
    }

    public KioskLoginResponse findCurrentStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));
        return KioskLoginResponse.fromEntity(store);
    }

    public void logout(Long storeId, String token) {
        tokenRedisService.removeKioskToken(storeId, token);
    }

    public record KioskLoginResult(String token, KioskLoginResponse storeInfo) { }
}
