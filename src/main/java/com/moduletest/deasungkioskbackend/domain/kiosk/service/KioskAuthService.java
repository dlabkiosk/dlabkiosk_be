package com.moduletest.deasungkioskbackend.domain.kiosk.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.security.TokenRedisService;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginRequest;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginResponse;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KioskAuthService {

    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

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

        KioskLoginResponse loginResponse = KioskLoginResponse.fromEntity(store);
        return new KioskLoginResult(token, loginResponse);
    }

    public void logout(Long storeId) {
        tokenRedisService.removeKioskToken(storeId);
    }

    public record KioskLoginResult(String token, KioskLoginResponse storeInfo) { }
}
