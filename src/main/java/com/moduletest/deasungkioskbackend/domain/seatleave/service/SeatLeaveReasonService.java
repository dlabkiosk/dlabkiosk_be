package com.moduletest.deasungkioskbackend.domain.seatleave.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeaveReason;
import com.moduletest.deasungkioskbackend.domain.seatleave.exception.SeatLeaveException;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveReasonRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatLeaveReasonService {


    private final SeatLeaveReasonRepository seatLeaveReasonRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;


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
    public SeatLeaveReasonResponse createReason(SeatLeaveReasonCreateRequest request,
        Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new SeatLeaveException(
                ErrorCode.STORE_NOT_FOUND));

        SeatLeaveReason reason = SeatLeaveReason.builder().store(store)
            .reasonName(request.reasonName())
            .displayOrder(request.displayOrder()).active(request.active()).build();

        seatLeaveReasonRepository.save(reason);
        return SeatLeaveReasonResponse.fromEntity(reason);

    }


    @Transactional
    public SeatLeaveReasonResponse updateReason(Long reasonId,
        SeatLeaveReasonUpdateRequest request) {
        SeatLeaveReason reason = seatLeaveReasonRepository.findById(reasonId)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_NOT_FOUND));
        reason.updateInfo(request.reasonName(), request.displayOrder(), request.active());
        return SeatLeaveReasonResponse.fromEntity(reason);
    }

    @Transactional
    public void deleteReason(Long id) {
        SeatLeaveReason reason = seatLeaveReasonRepository.findById(id)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_NOT_FOUND));
        seatLeaveReasonRepository.delete(reason);
    }
}
