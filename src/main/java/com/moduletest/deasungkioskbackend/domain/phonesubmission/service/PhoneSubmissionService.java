package com.moduletest.deasungkioskbackend.domain.phonesubmission.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmissionType;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.exception.PhoneSubmissionException;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId);

        LocalDate startDate;
        LocalDate endDate;

        switch (request.submissionType()) {
            case DAILY -> {
                startDate = LocalDate.now();
                endDate = LocalDate.now();
            }
            case PERIOD -> {
                startDate = request.startDate();
                endDate = request.endDate();
                if (startDate == null || endDate == null) {
                    throw new PhoneSubmissionException(ErrorCode.INVALID_PHONE_SUBMISSION_PERIOD);
                }
                if (startDate.isAfter(endDate)) {
                    throw new PhoneSubmissionException(ErrorCode.INVALID_PHONE_SUBMISSION_PERIOD);
                }
            }
            case NO_PHONE -> {
                startDate = LocalDate.now();
                endDate = null;
            }
            default -> throw new PhoneSubmissionException(ErrorCode.INVALID_INPUT_VALUE);
        }

        checkDuplicateSubmission(student.getId(), request.submissionType(), startDate, endDate);

        PhoneSubmission submission = PhoneSubmission.builder()
            .student(student)
            .store(student.getStore())
            .submittedAt(LocalDateTime.now())
            .submissionType(request.submissionType())
            .startDate(startDate)
            .endDate(endDate)
            .build();

        phoneSubmissionRepository.save(submission);

        return PhoneSubmissionResponse.fromEntity(submission);
    }

    private void checkDuplicateSubmission(Long studentId, PhoneSubmissionType type,
                                          LocalDate startDate, LocalDate endDate) {
        if (type == PhoneSubmissionType.NO_PHONE) {
            if (phoneSubmissionRepository.existsActiveNoPhone(studentId)) {
                throw new PhoneSubmissionException(ErrorCode.ALREADY_SUBMITTED_PHONE);
            }
        }

        LocalDate checkEnd = endDate != null ? endDate : LocalDate.of(9999, 12, 31);
        if (phoneSubmissionRepository.existsOverlapping(studentId, startDate, checkEnd)) {
            throw new PhoneSubmissionException(ErrorCode.ALREADY_SUBMITTED_PHONE);
        }
    }


    @Transactional
    public PhoneSubmissionResponse updatePhoneSubmission(Long id,
        PhoneSubmissionUpdateRequest request) {

        PhoneSubmission submission = phoneSubmissionRepository.findById(id)
            .orElseThrow(() -> new PhoneSubmissionException(ErrorCode.PHONE_SUBMISSION_NOT_FOUND));

        LocalDate startDate;
        LocalDate endDate;

        switch (request.submissionType()) {
            case DAILY -> {
                startDate = submission.getStartDate();
                endDate = submission.getStartDate();
            }
            case PERIOD -> {
                startDate = request.startDate();
                endDate = request.endDate();
                if (startDate == null || endDate == null) {
                    throw new PhoneSubmissionException(ErrorCode.INVALID_PHONE_SUBMISSION_PERIOD);
                }
                if (startDate.isAfter(endDate)) {
                    throw new PhoneSubmissionException(ErrorCode.INVALID_PHONE_SUBMISSION_PERIOD);
                }
            }
            case NO_PHONE -> {
                startDate = submission.getStartDate();
                endDate = null;
            }
            default -> throw new PhoneSubmissionException(ErrorCode.INVALID_INPUT_VALUE);
        }

        submission.updateSubmission(request.submissionType(), startDate, endDate, request.memo());

        return PhoneSubmissionResponse.fromEntity(submission);
    }

    @Transactional
    public void deletePhoneSubmission(Long id) {
        PhoneSubmission submission = phoneSubmissionRepository.findById(id)
            .orElseThrow(() -> new PhoneSubmissionException(ErrorCode.PHONE_SUBMISSION_NOT_FOUND));
        phoneSubmissionRepository.delete(submission);
    }

    public Page<PhoneSubmissionResponse> findAllByPeriod(Long storeId,
        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Page<PhoneSubmission> page;
        if (storeId != null) {
            page = phoneSubmissionRepository.findPageByStoreIdAndPeriod(
                storeId, start, end, pageable);
        } else {
            page = phoneSubmissionRepository.findPageByPeriod(start, end, pageable);
        }
        return page.map(PhoneSubmissionResponse::fromEntity);
    }

    public byte[] exportToExcel(Long storeId, LocalDate startDate, LocalDate endDate) {
        List<PhoneSubmission> submissions = findSubmissionsByPeriod(storeId, startDate, endDate);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("휴대폰 미소지 내역");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"번호", "학생명", "학번", "반", "배정좌석",
                "휴대폰 뒷자리", "학부모 전화번호", "신청유형", "시작일", "종료일", "메모", "신청시간"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PhoneSubmission ps : submissions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(ps.getStudent().getName());
                row.createCell(2).setCellValue(
                    ps.getStudent().getStudentNumber() != null
                        ? ps.getStudent().getStudentNumber() : "");
                row.createCell(3).setCellValue("");
                row.createCell(4).setCellValue(
                    ps.getStudent().getAssignedSeat() != null
                        ? ps.getStudent().getAssignedSeat().getSeatLabel() : "");
                row.createCell(5).setCellValue(
                    ps.getStudent().getPhoneLast4() != null
                        ? ps.getStudent().getPhoneLast4() : "");
                row.createCell(6).setCellValue("");
                row.createCell(7).setCellValue(formatSubmissionType(ps.getSubmissionType()));
                row.createCell(8).setCellValue(ps.getStartDate().toString());
                row.createCell(9).setCellValue(
                    ps.getEndDate() != null ? ps.getEndDate().toString() : "무기한");
                row.createCell(10).setCellValue(
                    ps.getMemo() != null ? ps.getMemo() : "");
                row.createCell(11).setCellValue(ps.getSubmittedAt().format(dtf));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new PhoneSubmissionException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String formatSubmissionType(PhoneSubmissionType type) {
        return switch (type) {
            case DAILY -> "당일";
            case PERIOD -> "기간";
            case NO_PHONE -> "미보유";
        };
    }

    private List<PhoneSubmission> findSubmissionsByPeriod(Long storeId,
        LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        if (storeId != null) {
            return phoneSubmissionRepository.findAllByStoreIdAndPeriod(storeId, start, end);
        }
        return phoneSubmissionRepository.findAllByPeriod(start, end);
    }

}
