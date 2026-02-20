package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentCreateRequest;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final QrCodeService qrCodeService;

    public List<StudentResponse> findAllStudents(Long storeId) {
        if (storeId != null) {
            return studentRepository.findAllByStoreId(storeId)
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
        }
        return studentRepository.findAll()
            .stream()
            .map(StudentResponse::fromEntity)
            .toList();
    }

    public StudentResponse findStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        Store store = storeRepository.findById(request.storeId())
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        if (studentRepository.existsByPhone(request.phone())) {
            throw new StudentException(ErrorCode.DUPLICATE_STUDENT_PHONE);
        }

        Student student = Student.builder()
            .store(store)
            .name(request.name())
            .phone(request.phone())
            .qrUuid(UUID.randomUUID().toString())
            .grade(request.grade())
            .build();

        Student savedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(savedStudent);
    }

    @Transactional
    public StudentResponse updateStudent(Long studentId, StudentUpdateRequest request) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));

        Store store = storeRepository.findById(request.storeId())
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        student.updateInfo(request.name(), request.phone(), request.grade(), store);
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        studentRepository.delete(student);
    }

    public byte[] generateStudentQrCode(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        return qrCodeService.generateQrCodePng(student.getQrUuid());
    }
}
