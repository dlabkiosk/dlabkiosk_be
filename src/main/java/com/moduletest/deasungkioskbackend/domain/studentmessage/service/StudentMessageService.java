package com.moduletest.deasungkioskbackend.domain.studentmessage.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageCreateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.MessageTemplate;
import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import com.moduletest.deasungkioskbackend.domain.studentmessage.exception.StudentMessageException;
import com.moduletest.deasungkioskbackend.domain.studentmessage.repository.MessageTemplateRepository;
import com.moduletest.deasungkioskbackend.domain.studentmessage.repository.StudentMessageRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentMessageService {

    private final StudentMessageRepository studentMessageRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private final StudentRepository studentRepository;

    public List<StudentMessageResponse> findAllByStudentId(Long studentId, Long storeId) {
        if (storeId != null) {
            validateStudentBelongsToStore(studentId, storeId);
        }
        return studentMessageRepository.findAllByStudentId(studentId)
            .stream()
            .map(StudentMessageResponse::fromEntity)
            .toList();
    }

    public List<StudentMessageResponse> findAllActiveByStudentId(Long studentId) {
        return studentMessageRepository.findAllActiveByStudentId(studentId)
            .stream()
            .map(StudentMessageResponse::fromEntity)
            .toList();
    }

    @Transactional
    public List<StudentMessageResponse> createMessage(StudentMessageCreateRequest request,
        Long storeId) {
        boolean hasTemplate = request.templateId() != null;
        boolean hasContent = request.content() != null && !request.content().isBlank();

        if (hasTemplate == hasContent) {
            throw new StudentMessageException(ErrorCode.STUDENT_MESSAGE_INVALID_REQUEST);
        }

        if (hasTemplate) {
            return createTemplateMessages(request.studentIds(), request.templateId(), storeId);
        }
        return createCustomMessages(request.studentIds(), request.content(), storeId);
    }

    private List<StudentMessageResponse> createTemplateMessages(List<Long> studentIds,
        Long templateId, Long storeId) {
        MessageTemplate template = messageTemplateRepository.findByIdWithStore(templateId)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.MESSAGE_TEMPLATE_NOT_FOUND));

        if (storeId != null && !template.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        List<StudentMessageResponse> responses = new ArrayList<>();
        for (Long studentId : studentIds) {
            Student student = studentRepository.findByIdWithStore(studentId)
                .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));

            if (storeId != null && !student.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            if (!student.getStore().getId().equals(template.getStore().getId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }

            if (studentMessageRepository.existsByStudentIdAndTemplateId(studentId, templateId)) {
                continue;
            }

            StudentMessage message = StudentMessage.ofTemplate(student, student.getStore(), template);
            studentMessageRepository.save(message);
            responses.add(StudentMessageResponse.fromEntity(message));
        }
        return responses;
    }

    private List<StudentMessageResponse> createCustomMessages(List<Long> studentIds,
        String content, Long storeId) {
        List<StudentMessageResponse> responses = new ArrayList<>();
        for (Long studentId : studentIds) {
            Student student = studentRepository.findByIdWithStore(studentId)
                .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));

            if (storeId != null && !student.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }

            StudentMessage message = StudentMessage.ofCustom(student, student.getStore(), content);
            studentMessageRepository.save(message);
            responses.add(StudentMessageResponse.fromEntity(message));
        }
        return responses;
    }

    @Transactional
    public StudentMessageResponse updateMessage(Long id,
        StudentMessageUpdateRequest request, Long storeId) {
        StudentMessage message = studentMessageRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.STUDENT_MESSAGE_NOT_FOUND));

        if (storeId != null && !message.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (message.isTemplateBased()) {
            throw new StudentMessageException(
                ErrorCode.STUDENT_MESSAGE_TEMPLATE_BASED_NOT_EDITABLE);
        }

        message.updateCustomContent(request.content(), request.active());
        return StudentMessageResponse.fromEntity(message);
    }

    @Transactional
    public void deleteMessage(Long id, Long storeId) {
        StudentMessage message = loadDeletableMessage(id, storeId);
        studentMessageRepository.delete(message);
    }

    @Transactional
    public void deleteMessages(List<Long> ids, Long storeId) {
        for (Long id : ids) {
            StudentMessage message = loadDeletableMessage(id, storeId);
            studentMessageRepository.delete(message);
        }
    }

    private StudentMessage loadDeletableMessage(Long id, Long storeId) {
        StudentMessage message = studentMessageRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.STUDENT_MESSAGE_NOT_FOUND));

        if (storeId != null && !message.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (message.isTemplateBased()) {
            throw new StudentMessageException(
                ErrorCode.STUDENT_MESSAGE_TEMPLATE_BASED_NOT_EDITABLE);
        }
        return message;
    }

    private void validateStudentBelongsToStore(Long studentId, Long storeId) {
        Student student = studentRepository.findByIdWithStore(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        if (!student.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
