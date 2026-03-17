package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.MultipleStudentsException;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.List;
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
        return studentRepository.findByRfidUid(identifier.trim())
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
    }

    public Student resolveBySeatLabel(String seatLabel, Long storeId) {
        if (seatLabel == null || seatLabel.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        return studentRepository.findBySeatLabelAndStoreId(seatLabel.trim(), storeId)
            .orElseThrow(() -> new StudentException(
                ErrorCode.STUDENT_NOT_FOUND_BY_SEAT_LABEL));
    }

    public Student resolveByPhoneLast4(String phoneLast4, Long storeId) {
        if (phoneLast4 == null || phoneLast4.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        List<Student> students = studentRepository.findAllByPhoneLast4AndStoreId(
            phoneLast4.trim(), storeId);
        if (students.isEmpty()) {
            throw new StudentException(ErrorCode.STUDENT_NOT_FOUND_BY_PHONE_LAST4);
        }
        if (students.size() > 1) {
            throw new MultipleStudentsException(students);
        }
        return students.get(0);
    }

    public Student resolveStudent(String identifier, String studentNumber) {
        if (identifier != null && !identifier.isBlank()) {
            return resolveByIdentifier(identifier);
        }
        if (studentNumber != null && !studentNumber.isBlank()) {
            return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_STUDENT_NUMBER));
        }
        throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
    }

    /**
     * 4가지 식별 방식 중 하나로 학생을 찾는다.
     * 우선순위: identifier(카드/QR) → studentNumber(학번) → seatLabel(좌석번호) → phoneLast4(폰뒷자리)
     */
    public Student resolve(String identifier, String studentNumber,
                           String seatLabel, String phoneLast4, Long storeId) {
        if (identifier != null && !identifier.isBlank()) {
            return resolveByIdentifier(identifier);
        }
        if (studentNumber != null && !studentNumber.isBlank()) {
            return studentRepository.findByStudentNumber(studentNumber.trim())
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_STUDENT_NUMBER));
        }
        if (seatLabel != null && !seatLabel.isBlank()) {
            return resolveBySeatLabel(seatLabel, storeId);
        }
        if (phoneLast4 != null && !phoneLast4.isBlank()) {
            return resolveByPhoneLast4(phoneLast4, storeId);
        }
        throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
    }

    /**
     * 값 하나로 학생을 자동 판별한다.
     * 순서: rfidUid → seatLabel → phoneLast4. 전부 없으면 에러.
     */
    public Student resolveAuto(String value, Long storeId) {
        if (value == null || value.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = value.trim();

        // 1. rfidUid로 조회
        Optional<Student> byRfid = studentRepository.findByRfidUid(trimmed);
        if (byRfid.isPresent()) {
            Student student = byRfid.get();
            if (!student.getStore().getId().equals(storeId)) {
                throw new StudentException(ErrorCode.STUDENT_NOT_IN_STORE);
            }
            return student;
        }

        // 2. seatLabel로 조회
        Optional<Student> bySeat = studentRepository.findBySeatLabelAndStoreId(
            trimmed, storeId);
        if (bySeat.isPresent()) {
            return bySeat.get();
        }

        // 3. phoneLast4로 조회
        List<Student> byPhone = studentRepository.findAllByPhoneLast4AndStoreId(
            trimmed, storeId);
        if (byPhone.size() == 1) {
            return byPhone.get(0);
        }
        if (byPhone.size() > 1) {
            throw new MultipleStudentsException(byPhone);
        }

        throw new StudentException(ErrorCode.STUDENT_NOT_FOUND);
    }
}
