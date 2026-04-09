package com.moduletest.deasungkioskbackend.domain.seatleave.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.common.service.S3Service;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeaveReason;
import com.moduletest.deasungkioskbackend.domain.seatleave.exception.SeatLeaveException;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveReasonRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatLeaveReasonService {

    private static final int MAX_REASONS_PER_STORE = 9;

    private final SeatLeaveReasonRepository seatLeaveReasonRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;
    private final S3Service s3Service;


    public List<SeatLeaveReasonResponse> findAllByStoreId(Long storeId) {
        if (storeId != null) {
            return seatLeaveReasonRepository.findAllByStoreIdWithStore(storeId).stream()
                .map(SeatLeaveReasonResponse::fromEntity).toList();
        }
        return seatLeaveReasonRepository.findAll().stream()
            .map(SeatLeaveReasonResponse::fromEntity).toList();
    }


    public List<SeatLeaveReasonResponse> findAllActiveByStoreId(Long storeId) {
        return seatLeaveReasonRepository.findAllActiveByStoreId(storeId).stream()
            .map(SeatLeaveReasonResponse::fromEntity).toList();
    }


    @Transactional
    public SeatLeaveReasonResponse createReason(Long storeId, MultipartFile iconFile,
        String reasonName, int displayOrder, boolean active) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.STORE_NOT_FOUND));

        if (seatLeaveReasonRepository.countByStoreId(storeId) >= MAX_REASONS_PER_STORE) {
            throw new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_LIMIT_EXCEEDED);
        }

        String iconUrl = (iconFile != null && !iconFile.isEmpty())
            ? s3Service.upload(iconFile) : null;

        SeatLeaveReason reason = SeatLeaveReason.builder()
            .store(store)
            .reasonName(reasonName)
            .displayOrder(displayOrder)
            .active(active)
            .iconUrl(iconUrl)
            .build();

        seatLeaveReasonRepository.save(reason);
        return SeatLeaveReasonResponse.fromEntity(reason);
    }


    @Transactional
    public SeatLeaveReasonResponse updateReason(Long reasonId, MultipartFile iconFile,
        String reasonName, int displayOrder, boolean active) {
        SeatLeaveReason reason = seatLeaveReasonRepository.findByIdWithStore(reasonId)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_NOT_FOUND));
        validateStoreAccess(reason);

        if (iconFile != null && !iconFile.isEmpty()) {
            if (reason.getIconUrl() != null) {
                s3Service.delete(reason.getIconUrl());
            }
            reason.replaceIcon(s3Service.upload(iconFile));
        }

        reason.updateInfo(reasonName, displayOrder, active);
        return SeatLeaveReasonResponse.fromEntity(reason);
    }

    @Transactional
    public void deleteReason(Long id) {
        SeatLeaveReason reason = seatLeaveReasonRepository.findByIdWithStore(id)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_NOT_FOUND));
        validateStoreAccess(reason);

        if (reason.getIconUrl() != null) {
            s3Service.delete(reason.getIconUrl());
        }
        seatLeaveReasonRepository.delete(reason);
    }

    private void validateStoreAccess(SeatLeaveReason reason) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!reason.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }
}
