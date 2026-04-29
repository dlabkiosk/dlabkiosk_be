package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.util.EmCardDecoder;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.MultipleStudentsException;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentResolverService {

    private final StudentRepository studentRepository;

    public Student resolveByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = identifier.trim();
        return findByRfidWithDecode(trimmed)
            .orElseThrow(() -> {
                log.warn("학생 식별 실패 [RFID]. value: {}", trimmed);
                return new StudentException(ErrorCode.STUDENT_NOT_FOUND);
            });
    }

    /**
     * rfidUid로 학생을 조회하되, 원본으로 못 찾으면 raw hex로 간주하고 디코딩 후 재조회.
     * QR(디코딩된 값)과 RFID 리더기 raw 둘 다 대응.
     */
    private Optional<Student> findByRfidWithDecode(String value) {
        Optional<Student> byRaw = studentRepository.findByRfidUid(value);
        if (byRaw.isPresent()) {
            return byRaw;
        }
        if (!EmCardDecoder.containsHexLetter(value)) {
            return Optional.empty();
        }
        Optional<String> decoded = EmCardDecoder.decode(value);
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        return studentRepository.findByRfidUid(decoded.get());
    }

    public Student resolveBySeatLabel(String seatLabel, Long storeId) {
        if (seatLabel == null || seatLabel.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = seatLabel.trim();
        return studentRepository.findBySeatLabelAndStoreId(trimmed, storeId)
            .orElseThrow(() -> {
                log.warn("학생 식별 실패 [SEAT]. value: {}, storeId: {}", trimmed, storeId);
                return new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_SEAT_LABEL);
            });
    }

    public Student resolveByPhone(String phone, Long storeId) {
        if (phone == null || phone.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String phone8 = phone.length() >= 8
            ? phone.substring(phone.length() - 8) : phone;
        List<Student> students = studentRepository.findAllByPhoneAndStoreId(
            phone8, storeId);
        if (students.isEmpty()) {
            log.warn("학생 식별 실패 [PHONE8]. value: {}, storeId: {}", phone8, storeId);
            throw new StudentException(ErrorCode.STUDENT_NOT_FOUND);
        }
        if (students.size() > 1) {
            throw new MultipleStudentsException(students);
        }
        return students.get(0);
    }

    public Student resolveByPhoneLast4(String phoneLast4, Long storeId) {
        if (phoneLast4 == null || phoneLast4.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = phoneLast4.trim();
        List<Student> students = studentRepository.findAllByPhoneLast4AndStoreId(
            trimmed, storeId);
        if (students.isEmpty()) {
            log.warn("학생 식별 실패 [PHONE_LAST4]. value: {}, storeId: {}",
                trimmed, storeId);
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
                .orElseThrow(() -> {
                    log.warn("학생 식별 실패 [STD_NO]. value: {}", studentNumber);
                    return new StudentException(
                        ErrorCode.STUDENT_NOT_FOUND_BY_STUDENT_NUMBER);
                });
        }
        throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
    }

    /**
     * 3가지 식별 방식 중 하나로 학생을 찾는다.
     * 우선순위: identifier(카드/QR) → studentNumber(학번) → phoneLast4(폰뒷자리)
     */
    public Student resolve(String identifier, String studentNumber,
                           String phoneLast4, Long storeId) {
        if (identifier != null && !identifier.isBlank()) {
            return resolveByIdentifier(identifier);
        }
        if (studentNumber != null && !studentNumber.isBlank()) {
            String trimmed = studentNumber.trim();
            return studentRepository.findByStudentNumber(trimmed)
                .orElseThrow(() -> {
                    log.warn("학생 식별 실패 [STD_NO]. value: {}, storeId: {}",
                        trimmed, storeId);
                    return new StudentException(
                        ErrorCode.STUDENT_NOT_FOUND_BY_STUDENT_NUMBER);
                });
        }
        if (phoneLast4 != null && !phoneLast4.isBlank()) {
            return resolveByPhoneLast4(phoneLast4, storeId);
        }
        throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
    }

    /**
     * inputMethod가 지정되면 해당 방식으로만 조회한다.
     * null이면 자동 판별 (rfidUid → phone 8자리 → phoneLast4).
     */
    public Student resolveAuto(String value, Long storeId, InputMethod inputMethod) {
        if (value == null || value.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        if (inputMethod != null) {
            Student student = switch (inputMethod) {
                case RFID -> resolveByIdentifier(value.trim());
                case PHONE_LAST4 -> resolveByPhoneLast4(value.trim(), storeId);
                case PHONE -> resolveByPhone(value.trim(), storeId);
            };
            if (!student.getStore().getId().equals(storeId)) {
                throw new StudentException(ErrorCode.STUDENT_NOT_IN_STORE);
            }
            return student;
        }
        return resolveAutoInternal(value, storeId);
    }

    public Student resolveAuto(String value, Long storeId) {
        return resolveAutoInternal(value, storeId);
    }

    private Student resolveAutoInternal(String value, Long storeId) {
        if (value == null || value.isBlank()) {
            throw new StudentException(ErrorCode.INVALID_STUDENT_IDENTIFIER);
        }
        String trimmed = value.trim();

        // 1. rfidUid(카드/QR)로 조회 — raw hex면 디코딩 후 재조회
        Optional<Student> byRfid = findByRfidWithDecode(trimmed);
        if (byRfid.isPresent()) {
            Student student = byRfid.get();
            if (!student.getStore().getId().equals(storeId)) {
                throw new StudentException(ErrorCode.STUDENT_NOT_IN_STORE);
            }
            return student;
        }

        // 2. 카드/QR이 아니면 전화번호 8자리로 조회
        return resolveByPhone(trimmed, storeId);
    }
}
