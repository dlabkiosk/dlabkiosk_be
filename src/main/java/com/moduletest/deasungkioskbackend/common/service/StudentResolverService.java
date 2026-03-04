package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentResolverService {

    private final StudentRepository studentRepository;

    public Student resolveByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = identifier.trim();

        Optional<Student> student = studentRepository.findByQrUuid(trimmed);
        if (student.isPresent()) {
            return student.get();
        }
        return studentRepository.findByRfidUid(trimmed)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
    }

    public Student resolveStudent(String identifier, String studentNumber, String phone) {
        if (identifier != null && !identifier.isBlank()) {
            return resolveByIdentifier(identifier);
        }
        if (studentNumber != null && !studentNumber.isBlank()) {
            return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_STUDENT_NUMBER));
        }
        if (phone != null && !phone.isBlank()) {
            return studentRepository.findByPhone(phone)
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_PHONE));
        }
        throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
    }
}
