package com.moduletest.deasungkioskbackend.domain.notice.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeCreateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeResponse;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.entity.Notice;
import com.moduletest.deasungkioskbackend.domain.notice.exception.NoticeException;
import com.moduletest.deasungkioskbackend.domain.notice.repository.NoticeRepository;
import com.moduletest.deasungkioskbackend.domain.noticecategory.entity.NoticeCategory;
import com.moduletest.deasungkioskbackend.domain.noticecategory.exception.NoticeCategoryException;
import com.moduletest.deasungkioskbackend.domain.noticecategory.repository.NoticeCategoryRepository;
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
    private final NoticeCategoryRepository noticeCategoryRepository;


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

        NoticeCategory category = resolveCategory(request.categoryId(), storeId);

        String createdBy = SecurityUtil.isAdmin()
            ? "전체관리자"
            : store.getStoreName();

        Notice notice = Notice.builder()
            .store(store)
            .category(category)
            .title(request.title())
            .content(request.content())
            .pinned(request.pinned() != null && request.pinned())
            .active(true)
            .createdBy(createdBy)
            .build();

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.fromEntity(savedNotice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findByIdWithStore(noticeId)
            .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
        validateStoreAccess(notice);

        notice.updateInfo(request.title(), request.content(), request.pinned(), request.active());

        if (Boolean.TRUE.equals(request.updateCategory())) {
            NoticeCategory category = resolveCategory(
                request.categoryId(), notice.getStore().getId());
            notice.changeCategory(category);
        }

        return NoticeResponse.fromEntity(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdWithStore(noticeId)
            .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
        validateStoreAccess(notice);
        noticeRepository.delete(notice);
    }

    private NoticeCategory resolveCategory(Long categoryId, Long storeId) {
        if (categoryId == null) {
            return null;
        }
        NoticeCategory category = noticeCategoryRepository.findByIdWithStore(categoryId)
            .orElseThrow(() ->
                new NoticeCategoryException(ErrorCode.NOTICE_CATEGORY_NOT_FOUND));
        if (!category.getStore().getId().equals(storeId)) {
            throw new NoticeCategoryException(ErrorCode.NOTICE_CATEGORY_STORE_MISMATCH);
        }
        return category;
    }

    private void validateStoreAccess(Notice notice) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!notice.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }
}
