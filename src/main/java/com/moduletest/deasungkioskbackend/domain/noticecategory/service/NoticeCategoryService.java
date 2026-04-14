package com.moduletest.deasungkioskbackend.domain.noticecategory.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.notice.repository.NoticeRepository;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryCreateRequest;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryOrderRequest;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryResponse;
import com.moduletest.deasungkioskbackend.domain.noticecategory.entity.NoticeCategory;
import com.moduletest.deasungkioskbackend.domain.noticecategory.exception.NoticeCategoryException;
import com.moduletest.deasungkioskbackend.domain.noticecategory.repository.NoticeCategoryRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeCategoryService {

    private final NoticeCategoryRepository noticeCategoryRepository;
    private final NoticeRepository noticeRepository;
    private final StoreRepository storeRepository;

    public List<NoticeCategoryResponse> findAllByStoreId(Long storeId) {
        return noticeCategoryRepository.findAllByStoreIdOrdered(storeId)
            .stream()
            .map(NoticeCategoryResponse::fromEntity)
            .toList();
    }

    @Transactional
    public NoticeCategoryResponse create(NoticeCategoryCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        String name = request.name().trim();
        if (noticeCategoryRepository.existsByStoreIdAndName(storeId, name)) {
            throw new NoticeCategoryException(ErrorCode.NOTICE_CATEGORY_DUPLICATE_NAME);
        }

        int nextOrder = noticeCategoryRepository.findMaxDisplayOrderByStoreId(storeId) + 1;

        NoticeCategory category = NoticeCategory.builder()
            .store(store)
            .name(name)
            .displayOrder(nextOrder)
            .build();

        return NoticeCategoryResponse.fromEntity(noticeCategoryRepository.save(category));
    }

    @Transactional
    public void delete(Long categoryId) {
        NoticeCategory category = noticeCategoryRepository.findByIdWithStore(categoryId)
            .orElseThrow(() -> new NoticeCategoryException(ErrorCode.NOTICE_CATEGORY_NOT_FOUND));
        validateStoreAccess(category);

        if (noticeRepository.existsByCategoryId(categoryId)) {
            throw new NoticeCategoryException(ErrorCode.NOTICE_CATEGORY_IN_USE);
        }

        noticeCategoryRepository.delete(category);
    }

    @Transactional
    public List<NoticeCategoryResponse> reorder(NoticeCategoryOrderRequest request, Long storeId) {
        List<NoticeCategory> existing = noticeCategoryRepository.findAllByStoreIdOrdered(storeId);

        Set<Long> existingIds = existing.stream()
            .map(NoticeCategory::getId)
            .collect(Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(request.orderedIds());

        if (!existingIds.equals(requestedIds)
            || requestedIds.size() != request.orderedIds().size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Map<Long, NoticeCategory> byId = existing.stream()
            .collect(Collectors.toMap(NoticeCategory::getId, Function.identity()));

        int order = 1;
        for (Long id : request.orderedIds()) {
            byId.get(id).changeOrder(order++);
        }

        return findAllByStoreId(storeId);
    }

    private void validateStoreAccess(NoticeCategory category) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!category.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }
}
