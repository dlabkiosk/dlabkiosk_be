package com.moduletest.deasungkioskbackend.domain.advertisement.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.FileStorageService;
import com.moduletest.deasungkioskbackend.domain.advertisement.dto.AdvertisementResponse;
import com.moduletest.deasungkioskbackend.domain.advertisement.entity.Advertisement;
import com.moduletest.deasungkioskbackend.domain.advertisement.exception.AdvertisementException;
import com.moduletest.deasungkioskbackend.domain.advertisement.repository.AdvertisementRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final StoreRepository storeRepository;
    private final FileStorageService fileStorageService;

    public List<AdvertisementResponse> findAllAdvertisements(Long storeId) {
        if (storeId != null) {
            return advertisementRepository.findAllByStoreIdWithStore(storeId).stream()
                .map(AdvertisementResponse::fromEntity)
                .toList();
        }
        return advertisementRepository.findAllWithStore().stream()
            .map(AdvertisementResponse::fromEntity)
            .toList();
    }

    public AdvertisementResponse findAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdvertisementException(ErrorCode.ADVERTISEMENT_NOT_FOUND));
        return AdvertisementResponse.fromEntity(advertisement);
    }

    public List<AdvertisementResponse> findActiveAdvertisementsByStoreId(Long storeId) {
        return advertisementRepository.findAllActiveByStoreIdWithStore(storeId).stream()
            .map(AdvertisementResponse::fromEntity)
            .toList();
    }

    @Transactional
    public AdvertisementResponse createAdvertisement(Long storeId, MultipartFile file,
                                                     String mediaType, int displayOrder,
                                                     int displaySeconds) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        String fileUrl = fileStorageService.store(file);

        Advertisement advertisement = Advertisement.builder()
            .store(store)
            .imageUrl(fileUrl)
            .mediaType(mediaType)
            .displayOrder(displayOrder)
            .displaySeconds(displaySeconds > 0 ? displaySeconds : 5)
            .active(true)
            .build();

        advertisementRepository.save(advertisement);
        return AdvertisementResponse.fromEntity(advertisement);
    }

    @Transactional
    public AdvertisementResponse updateAdvertisement(Long id, MultipartFile file,
                                                     String mediaType, Integer displayOrder,
                                                     Integer displaySeconds, Boolean active) {
        Advertisement advertisement = advertisementRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdvertisementException(ErrorCode.ADVERTISEMENT_NOT_FOUND));

        String newImageUrl = null;
        if (file != null && !file.isEmpty()) {
            fileStorageService.delete(advertisement.getImageUrl());
            newImageUrl = fileStorageService.store(file);
        }

        advertisement.updateInfo(newImageUrl, mediaType, displayOrder, displaySeconds, active);
        return AdvertisementResponse.fromEntity(advertisement);
    }

    @Transactional
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementException(ErrorCode.ADVERTISEMENT_NOT_FOUND));
        fileStorageService.delete(advertisement.getImageUrl());
        advertisementRepository.delete(advertisement);
    }
}
