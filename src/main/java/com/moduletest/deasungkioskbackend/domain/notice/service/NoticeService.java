package com.moduletest.deasungkioskbackend.domain.notice.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeCreateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeResponse;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.entity.Notice;
import com.moduletest.deasungkioskbackend.domain.notice.exception.NoticeException;
import com.moduletest.deasungkioskbackend.domain.notice.repository.NoticeRepository;
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
public class NoticeService {


    private final NoticeRepository noticeRepository;
    private final StoreRepository storeRepository;


    public List<NoticeResponse> findAllNotices() {
        return noticeRepository.findAllWithStore()
            .stream()
            .map(NoticeResponse::fromEntity)
            .toList();
    }

    public List<NoticeResponse> findAllNoticesByStoreId(Long storeId) {
        return noticeRepository.findAllByStoreIdWithStore(storeId)
            .stream()
            .map(NoticeResponse::fromEntity)
            .toList();
    }


    public List<NoticeResponse> findActiveNoticesByStoreId(Long storeId) {
        return noticeRepository.findAllActiveByStoreIdWithStore(storeId)
            .stream()
            .map(NoticeResponse::fromEntity)
            .toList();
    }

    public NoticeResponse findNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findByIdWithStore(noticeId)
            .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeResponse.fromEntity(notice);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        Notice notice = Notice.builder()
            .store(store)
            .title(request.title())
            .content(request.content())
            .pinned(request.pinned() != null && request.pinned())
            .active(true)
            .build();

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.fromEntity(savedNotice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findByIdWithStore(noticeId)
            .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));

        notice.updateInfo(request.title(), request.content(), request.pinned(), request.active());
        return NoticeResponse.fromEntity(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
            .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
        noticeRepository.delete(notice);
    }
}
