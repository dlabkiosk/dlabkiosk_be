package com.moduletest.deasungkioskbackend.domain.phonesubmission.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.exception.PhoneSubmissionException;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhoneSubmissionService {

    private final PhoneSubmissionRepository phoneSubmissionRepository;
    private final StudentResolverService studentResolverService;


    @Transactional
    public PhoneSubmissionResponse submitPhoneNonPossession(PhoneSubmissionRequest request,
        Long storeId) {

        Student student = studentResolverService.resolveByIdentifier(request.identifier());
        validateStudentStore(student, storeId);

        LocalDateTime localDateTime = LocalDate.now().atStartOfDay();

        boolean alreadySubmitted = phoneSubmissionRepository.existsByStudentIdAndSubmittedAtGreaterThanEqual(
            student.getId(), localDateTime);

        if (alreadySubmitted) {
            throw new PhoneSubmissionException(ErrorCode.ALREADY_SUBMITTED_PHONE);
        }

        PhoneSubmission submission = PhoneSubmission.builder()
            .student(student)
            .store(student.getStore())
            .submittedAt(LocalDateTime.now())
            .build();

        phoneSubmissionRepository.save(submission);

        return PhoneSubmissionResponse.fromEntity(submission);

    }


    public List<PhoneSubmissionResponse> findAllByStoreIdToday(Long storeId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return phoneSubmissionRepository.findAllByStoreIdToday(storeId, startOfDay)
            .stream()
            .map(PhoneSubmissionResponse::fromEntity)
            .toList();
    }

    private void validateStudentStore(Student student, Long storeId) {
        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }
    }


}
