package com.moduletest.deasungkioskbackend.domain.advertisement.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.common.service.ImageCropService;
import com.moduletest.deasungkioskbackend.common.service.S3Service;
import java.io.InputStream;
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
    private final S3Service s3Service;
    private final ImageCropService imageCropService;

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
        validateStoreAccess(advertisement);
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
        int displaySeconds,
        Integer cropX, Integer cropY,
        Integer cropWidth, Integer cropHeight) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        String fileUrl = uploadWithCrop(file, cropX, cropY, cropWidth, cropHeight);

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
        Integer displaySeconds, Boolean active,
        Integer cropX, Integer cropY,
        Integer cropWidth, Integer cropHeight) {
        Advertisement advertisement = advertisementRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdvertisementException(ErrorCode.ADVERTISEMENT_NOT_FOUND));
        validateStoreAccess(advertisement);

        String newImageUrl = null;
        if (file != null && !file.isEmpty()) {
            s3Service.delete(advertisement.getImageUrl());
            newImageUrl = uploadWithCrop(file, cropX, cropY, cropWidth, cropHeight);
        }

        advertisement.updateInfo(newImageUrl, mediaType, displayOrder, displaySeconds, active);
        return AdvertisementResponse.fromEntity(advertisement);
    }

    private String uploadWithCrop(MultipartFile file,
                                   Integer cropX, Integer cropY,
                                   Integer cropWidth, Integer cropHeight) {
        if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
            long croppedSize = imageCropService.getCroppedSize(
                file, cropX, cropY, cropWidth, cropHeight);
            InputStream croppedStream = imageCropService.cropImage(
                file, cropX, cropY, cropWidth, cropHeight);

            String extension = extractExtension(file.getOriginalFilename());
            return s3Service.upload(croppedStream, croppedSize,
                file.getContentType(), extension);
        }
        return s3Service.upload(file);
    }

    private void validateStoreAccess(Advertisement advertisement) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!advertisement.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".png";
    }

    @Transactional
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdvertisementException(ErrorCode.ADVERTISEMENT_NOT_FOUND));
        validateStoreAccess(advertisement);
        s3Service.delete(advertisement.getImageUrl());
        advertisementRepository.delete(advertisement);
    }
}
