package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentResolverService {

    private final StudentRepository studentRepository;

    public Student resolveStudent(String qrUuid, String rfidUid,
                                  String studentNumber, String phone) {
        if (qrUuid != null && !qrUuid.isBlank()) {
            return studentRepository.findByQrUuid(qrUuid)
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_QR));
        }
        if (rfidUid != null && !rfidUid.isBlank()) {
            return studentRepository.findByRfidUid(rfidUid)
                .orElseThrow(() -> new StudentException(
                    ErrorCode.STUDENT_NOT_FOUND_BY_RFID));
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
