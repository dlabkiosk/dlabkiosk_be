package com.moduletest.deasungkioskbackend.domain.seatleave.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveEndRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveStartRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeave;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeaveReason;
import com.moduletest.deasungkioskbackend.domain.seatleave.exception.SeatLeaveException;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveReasonRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeRedisService;
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
public class SeatLeaveService {

    private final SeatLeaveRepository seatLeaveRepository;
    private final SeatLeaveReasonRepository seatLeaveReasonRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;
    private final StudentResolverService studentResolverService;
    private final StudyTimeRedisService studyTimeRedisService;

    @Transactional
    public SeatLeaveResponse startLeave(Long storeId, SeatLeaveStartRequest request) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // 이미 이탈 중인지 확인
        seatLeaveRepository.findActiveSeatLeaveByStudentToday(student.getId(), startOfDay)
            .ifPresent(sl -> {
                throw new SeatLeaveException(ErrorCode.ALREADY_ON_SEAT_LEAVE);
            });

        // 사용 중인 좌석 확인
        SeatUsage seatUsage = seatUsageRepository
            .findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.NO_ACTIVE_SEAT));

        // 이탈 사유 확인
        SeatLeaveReason reason = seatLeaveReasonRepository.findById(request.reasonId())
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.SEAT_LEAVE_REASON_NOT_FOUND));

        // DB 저장
        SeatLeave seatLeave = SeatLeave.builder()
            .student(student)
            .store(seatUsage.getStore())
            .seat(seatUsage.getSeat())
            .reason(reason)
            .startedAt(LocalDateTime.now())
            .build();
        seatLeaveRepository.save(seatLeave);

        // Redis 상태 변경 (IN_USE → AWAY)
        seatRedisService.markSeatAway(
            storeId, seatUsage.getSeat().getId(),
            student.getId(), student.getName());

        return SeatLeaveResponse.fromEntity(seatLeave);
    }

    @Transactional
    public SeatLeaveResponse endLeave(Long storeId, SeatLeaveEndRequest request) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        SeatLeave seatLeave = seatLeaveRepository
            .findActiveSeatLeaveByStudentToday(student.getId(), startOfDay)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.NOT_ON_SEAT_LEAVE));

        seatLeave.endLeave();

        studyTimeRedisService.addSeatLeaveDeduction(
            storeId, student.getId(),
            seatLeave.getStartedAt(), seatLeave.getEndedAt());

        // Redis 상태 복원 (AWAY → IN_USE)
        seatRedisService.markSeatInUse(
            storeId, seatLeave.getSeat().getId(),
            student.getId(), student.getName());

        return SeatLeaveResponse.fromEntity(seatLeave);
    }

    @Transactional
    public SeatLeaveResponse forceEndLeave(Long seatLeaveId) {
        SeatLeave seatLeave = seatLeaveRepository.findByIdWithDetails(seatLeaveId)
            .orElseThrow(() -> new SeatLeaveException(ErrorCode.NOT_ON_SEAT_LEAVE));
        validateStoreAccess(seatLeave);

        if (seatLeave.getEndedAt() != null) {
            throw new SeatLeaveException(ErrorCode.NOT_ON_SEAT_LEAVE);
        }

        seatLeave.endLeave();

        Long storeId = seatLeave.getStore().getId();
        Long studentId = seatLeave.getStudent().getId();

        studyTimeRedisService.addSeatLeaveDeduction(
            storeId, studentId,
            seatLeave.getStartedAt(), seatLeave.getEndedAt());

        seatRedisService.markSeatInUse(
            storeId, seatLeave.getSeat().getId(),
            studentId, seatLeave.getStudent().getName());

        return SeatLeaveResponse.fromEntity(seatLeave);
    }

    public Page<SeatLeaveResponse> findAllByPeriod(Long storeId,
        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Page<SeatLeave> page;
        if (storeId != null) {
            page = seatLeaveRepository.findPageByStoreIdAndPeriod(
                storeId, start, end, pageable);
        } else {
            page = seatLeaveRepository.findPageByPeriod(start, end, pageable);
        }
        return page.map(SeatLeaveResponse::fromEntity);
    }

    public byte[] exportToExcel(Long storeId, LocalDate startDate, LocalDate endDate) {
        List<SeatLeave> leaves = findLeavesByPeriod(storeId, startDate, endDate);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("좌석이탈 내역");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"번호", "학생명", "학번", "좌석", "이탈사유",
                "이탈시간", "복귀시간", "이탈시간(분)"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (SeatLeave sl : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(sl.getStudent().getName());
                row.createCell(2).setCellValue(
                    sl.getStudent().getStudentNumber() != null
                        ? sl.getStudent().getStudentNumber() : "");
                row.createCell(3).setCellValue(sl.getSeat().getSeatLabel());
                row.createCell(4).setCellValue(sl.getReason().getReasonName());
                row.createCell(5).setCellValue(sl.getStartedAt().format(dtf));
                row.createCell(6).setCellValue(
                    sl.getEndedAt() != null ? sl.getEndedAt().format(dtf) : "이탈 중");

                if (sl.getEndedAt() != null) {
                    long minutes = java.time.Duration.between(
                        sl.getStartedAt(), sl.getEndedAt()).toMinutes();
                    row.createCell(7).setCellValue(minutes);
                } else {
                    row.createCell(7).setCellValue("-");
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new SeatLeaveException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateStoreAccess(SeatLeave seatLeave) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!seatLeave.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    private List<SeatLeave> findLeavesByPeriod(Long storeId,
        LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        if (storeId != null) {
            return seatLeaveRepository.findAllByStoreIdAndPeriod(storeId, start, end);
        }
        return seatLeaveRepository.findAllByPeriod(start, end);
    }
}
