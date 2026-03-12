package com.moduletest.deasungkioskbackend.domain.phonesubmission.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

        Student student = studentResolverService.resolveBySeatLabel(
            request.seatLabel(), storeId);

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
            String[] columns = {"번호", "학생명", "학번", "배정좌석", "신청시간"};
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
                row.createCell(3).setCellValue(
                    ps.getStudent().getAssignedSeat() != null
                        ? ps.getStudent().getAssignedSeat().getSeatLabel() : "");
                row.createCell(4).setCellValue(ps.getSubmittedAt().format(dtf));
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
