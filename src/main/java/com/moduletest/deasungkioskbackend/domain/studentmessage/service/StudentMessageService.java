package com.moduletest.deasungkioskbackend.domain.studentmessage.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageCreateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import com.moduletest.deasungkioskbackend.domain.studentmessage.exception.StudentMessageException;
import com.moduletest.deasungkioskbackend.domain.studentmessage.repository.StudentMessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentMessageService {

    private final StudentMessageRepository studentMessageRepository;
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
    public StudentMessageResponse createMessage(StudentMessageCreateRequest request,
        Long storeId) {
        Student student = studentRepository.findByIdWithStore(request.studentId())
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));

        if (storeId != null && !student.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        StudentMessage message = StudentMessage.builder()
            .student(student)
            .store(student.getStore())
            .content(request.content())
            .build();

        studentMessageRepository.save(message);
        return StudentMessageResponse.fromEntity(message);
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

        message.updateInfo(request.content(), request.active());
        return StudentMessageResponse.fromEntity(message);
    }

    @Transactional
    public void deleteMessage(Long id, Long storeId) {
        StudentMessage message = studentMessageRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new StudentMessageException(
                ErrorCode.STUDENT_MESSAGE_NOT_FOUND));

        if (storeId != null && !message.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        studentMessageRepository.delete(message);
    }

    private void validateStudentBelongsToStore(Long studentId, Long storeId) {
        Student student = studentRepository.findByIdWithStore(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        if (!student.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
