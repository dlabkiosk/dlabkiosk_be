package com.moduletest.deasungkioskbackend.domain.studentmessage.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateCreateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.MessageTemplate;
import com.moduletest.deasungkioskbackend.domain.studentmessage.exception.StudentMessageException;
import com.moduletest.deasungkioskbackend.domain.studentmessage.repository.MessageTemplateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;
    private final StoreRepository storeRepository;

    public List<MessageTemplateResponse> findAllTemplates(Long storeId) {
        if (storeId != null) {
            return messageTemplateRepository.findAllByStoreIdWithStore(storeId)
                .stream()
                .map(MessageTemplateResponse::fromEntity)
                .toList();
        }
        return messageTemplateRepository.findAllWithStore()
            .stream()
            .map(MessageTemplateResponse::fromEntity)
            .toList();
    }

    @Transactional
    public MessageTemplateResponse createTemplate(MessageTemplateCreateRequest request,
                                                   Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        MessageTemplate template = MessageTemplate.builder()
            .store(store)
            .content(request.content())
            .build();

        messageTemplateRepository.save(template);
        return MessageTemplateResponse.fromEntity(template);
    }

    @Transactional
    public MessageTemplateResponse updateTemplate(Long id,
        MessageTemplateUpdateRequest request) {
        MessageTemplate template = messageTemplateRepository.findByIdWithStore(id)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.MESSAGE_TEMPLATE_NOT_FOUND));
        validateStoreAccess(template);

        template.updateContent(request.content());
        return MessageTemplateResponse.fromEntity(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        MessageTemplate template = messageTemplateRepository.findByIdWithStore(id)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.MESSAGE_TEMPLATE_NOT_FOUND));
        validateStoreAccess(template);
        messageTemplateRepository.delete(template);
    }

    private void validateStoreAccess(MessageTemplate template) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!template.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }
}
