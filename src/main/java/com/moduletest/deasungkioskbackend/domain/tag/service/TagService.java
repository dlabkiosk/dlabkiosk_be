package com.moduletest.deasungkioskbackend.domain.tag.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAttendanceService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAttendanceService.AttendTagResult;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaMealService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaRequestService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaRequestService.ApprovedRequest;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import com.moduletest.deasungkioskbackend.domain.attendance.exception.AttendanceException;
import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.kiosk.exception.KioskException;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealTag;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealTagRepository;
import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeave;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import com.moduletest.deasungkioskbackend.domain.studentmessage.repository.StudentMessageRepository;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeRedisService;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagConfirmRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse.MealInfo;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse.PendingAction;
import com.moduletest.deasungkioskbackend.domain.tag.entity.AttendAction;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final StudentResolverService studentResolverService;
    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final AttendanceRepository attendanceRepository;
    private final OutingRepository outingRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;
    private final StudyTimeRedisService studyTimeRedisService;
    private final StudentMessageRepository studentMessageRepository;
    private final DsaAttendanceService dsaAttendanceService;
    private final DsaMealService dsaMealService;
    private final DsaRequestService dsaRequestService;
    private final MealTagRepository mealTagRepository;
    private final SeatLeaveRepository seatLeaveRepository;

    @Transactional
    public TagResponse processTag(TagRequest request, Long storeId) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());
        validateStudentStore(student, storeId);
        // 비관적 락: 동일 학생 동시 태그 방지
        studentRepository.findByIdForUpdate(student.getId());

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        MealType mealType = MealType.fromCurrentTime(LocalTime.now());
        MealInfo mealInfo = buildMealInfo(mealType, student, store, today);

        // 1. 외출 중이면 → 복귀 처리 우선 (+ 급식 정보 포함)
        Optional<Outing> activeOuting = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay);
        if (activeOuting.isPresent()) {
            AttendTagResult dsaResult = dsaAttendanceService.sendAttendTag(
                student.getRfidUid(), store);
            TagResponse response = handleOutingEnd(
                AttendAction.R, student, storeId, dsaResult.dsaSynced());
            return withMealInfo(response, mealInfo);
        }

        // 2. 좌석이탈 중이면 → 복귀 처리 (+ 급식 정보 포함)
        Optional<SeatLeave> activeSeatLeave = seatLeaveRepository
            .findActiveSeatLeaveByStudentToday(student.getId(), startOfDay);
        if (activeSeatLeave.isPresent()) {
            TagResponse response = handleSeatLeaveEnd(
                activeSeatLeave.get(), student, storeId);
            return withMealInfo(response, mealInfo);
        }

        // 3. 미등원이면 → 등원 처리 우선 (+ 급식 정보 포함)
        Optional<Attendance> checkedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN);
        if (checkedIn.isEmpty()) {
            AttendTagResult dsaResult = dsaAttendanceService.sendAttendTag(
                student.getRfidUid(), store);

            // 조퇴 후 재태그 → DSA 3.22 재등원 처리
            if (dsaResult.earlyLeftBlocked()) {
                TagResponse response = handleReAttendAfterEarlyLeave(
                    student, store, storeId);
                return withMealInfo(response, mealInfo);
            }

            AttendAction action = (dsaResult.action() == AttendAction.A)
                ? AttendAction.A : AttendAction.S;
            TagResponse response = handleCheckIn(
                action, student, storeId, dsaResult.dsaSynced());
            return withMealInfo(response, mealInfo);
        }

        // 3. 등원 상태 + 식사시간 → 급식 체크 우선, 외출/조퇴/하원은 pendingActions로
        if (mealType != null) {
            return handleMealTimeTag(mealType, mealInfo, student, store, storeId,
                startOfDay, endOfDay);
        }

        // 4. 식사시간 아님 → 기존 로직 (승인신청 확인 → 하원)
        return handleCheckedInTag(student, store, storeId, startOfDay, endOfDay, null);
    }

    private TagResponse handleMealTimeTag(MealType mealType, MealInfo mealInfo,
                                          Student student, Store store, Long storeId,
                                          LocalDateTime startOfDay, LocalDateTime endOfDay) {
        // 승인된 외출/조퇴 신청 확인
        List<ApprovedRequest> approvedRequests = dsaRequestService
            .findApprovedRequests(student.getRfidUid(), store);

        List<PendingAction> pendingActions = new ArrayList<>();

        // 외출/조퇴 승인 있으면 급식 먼저 → 외출/조퇴
        if (!approvedRequests.isEmpty()) {
            pendingActions.addAll(buildPendingActions(approvedRequests));
        } else {
            // 승인 없으면 급식 먼저 → 하원
            pendingActions.add(new PendingAction(AttendAction.T, "하원 하시겠습니까?", null));
        }

        return TagResponse.builder()
            .processed(false)
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .pendingActions(pendingActions)
            .dsaSynced(true)
            .mealInfo(mealInfo)
            .build();
    }

    private TagResponse handleCheckedInTag(Student student, Store store, Long storeId,
                                           LocalDateTime startOfDay, LocalDateTime endOfDay,
                                           MealInfo mealInfo) {
        List<ApprovedRequest> approvedRequests = dsaRequestService
            .findApprovedRequests(student.getRfidUid(), store);

        if (!approvedRequests.isEmpty()) {
            List<PendingAction> pendingActions = buildPendingActions(approvedRequests);
            return TagResponse.builder()
                .processed(false)
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentNumber())
                .seatLabel(getSeatLabel(student))
                .pendingActions(pendingActions)
                .dsaSynced(true)
                .mealInfo(mealInfo)
                .build();
        }

        // 승인된 신청 없음 → 하원 처리
        AttendTagResult checkOutResult = dsaAttendanceService.sendAttendTag(
            student.getRfidUid(), store);
        TagResponse response = handleCheckOut(
            AttendAction.T, student, storeId, checkOutResult.dsaSynced());
        return withMealInfo(response, mealInfo);
    }

    @Transactional
    public TagResponse confirmTag(TagConfirmRequest request, Long storeId) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());
        validateStudentStore(student, storeId);
        // 비관적 락: 동일 학생 동시 태그 방지
        studentRepository.findByIdForUpdate(student.getId());

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));

        // 외출/조퇴 시 사유신청권 재확인
        if (request.action() == AttendAction.D || request.action() == AttendAction.N
                || request.action() == AttendAction.C) {
            validateApprovedRequest(student, store, request.action());
        }

        // DSA 3.14에 태그 전송 — 실패 시 처리 중단
        AttendTagResult confirmResult = dsaAttendanceService.sendAttendTag(
            student.getRfidUid(), store);
        if (!confirmResult.dsaSynced()) {
            throw new AttendanceException(ErrorCode.DSA_SYNC_FAILED);
        }

        return switch (request.action()) {
            case D, N -> handleOutingStart(
                request.action(), student, storeId, true);
            case C -> handleCheckOut(
                AttendAction.C, student, storeId, true);
            default -> throw new AttendanceException(ErrorCode.INVALID_INPUT_VALUE);
        };
    }

    private void validateApprovedRequest(Student student, Store store, AttendAction action) {
        List<ApprovedRequest> approved = dsaRequestService
            .findApprovedRequests(student.getRfidUid(), store);

        if (approved.isEmpty()) {
            throw new AttendanceException(ErrorCode.OUTING_NOT_APPROVED);
        }

        boolean hasMatchingApproval = approved.stream()
            .anyMatch(req -> matchesAction(req.regGn(), action)
                && (action == AttendAction.C || isWithin30Minutes(req.regDt())));

        if (!hasMatchingApproval) {
            throw new AttendanceException(ErrorCode.OUTING_NOT_APPROVED);
        }
    }

    private boolean matchesAction(String regGn, AttendAction action) {
        if (regGn == null) {
            return true;
        }
        if (action == AttendAction.C) {
            return "6".equals(regGn) || "C".equalsIgnoreCase(regGn)
                || "조퇴".equals(regGn);
        }
        if (action == AttendAction.D || action == AttendAction.N) {
            return "4".equals(regGn) || "7".equals(regGn)
                || "D".equalsIgnoreCase(regGn) || "N".equalsIgnoreCase(regGn)
                || "외출".equals(regGn);
        }
        return true;
    }

    private boolean isWithin30Minutes(String regDt) {
        if (regDt == null || regDt.isBlank()) {
            return true;
        }
        try {
            LocalDateTime requestedTime = LocalDateTime.parse(regDt,
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();
            long minutesUntil = Duration.between(now, requestedTime).toMinutes();
            return minutesUntil <= 30;
        } catch (Exception e) {
            log.warn("reg_dt 파싱 실패: {}", regDt);
            return true;
        }
    }

    @Transactional
    public TagResponse confirmMealTag(String value, Long storeId,
                                      InputMethod inputMethod) {
        Student student = studentResolverService.resolveAuto(value, storeId, inputMethod);
        validateStudentStore(student, storeId);

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));

        MealType mealType = MealType.fromCurrentTime(LocalTime.now());
        if (mealType == null) {
            throw new AttendanceException(ErrorCode.NOT_MEAL_TIME);
        }

        LocalDate today = LocalDate.now();

        if (mealTagRepository.existsByStudentIdAndMealDateAndMealType(
                student.getId(), today, mealType)) {
            throw new AttendanceException(ErrorCode.ALREADY_MEAL_TAGGED);
        }

        boolean applied = dsaMealService.isMealApplied(
            student.getRfidUid(), mealType, store);

        if (!applied) {
            return TagResponse.builder()
                .processed(true)
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentNumber())
                .seatLabel(getSeatLabel(student))
                .mealInfo(new MealInfo(mealType, mealType.getLabel(), false, false,
                    mealType.getLabel() + " 신청내역 없음. 교직원에게 문의하세요!"))
                .build();
        }

        MealTag mealTag = MealTag.builder()
            .student(student)
            .store(student.getStore())
            .mealType(mealType)
            .mealDate(today)
            .taggedAt(LocalDateTime.now())
            .build();
        mealTagRepository.save(mealTag);

        return TagResponse.builder()
            .processed(true)
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .mealInfo(new MealInfo(mealType, mealType.getLabel(), true, false,
                "확인되었습니다."))
            .build();
    }

    // ── 급식 헬퍼 ──

    private MealInfo buildMealInfo(MealType mealType, Student student,
                                   Store store, LocalDate today) {
        if (mealType == null) {
            return null;
        }

        boolean alreadyTagged = mealTagRepository.existsByStudentIdAndMealDateAndMealType(
            student.getId(), today, mealType);

        if (alreadyTagged) {
            return new MealInfo(mealType, mealType.getLabel(), true, true,
                mealType.getLabel() + " 태그 완료");
        }

        boolean applied = dsaMealService.isMealApplied(
            student.getRfidUid(), mealType, store);

        if (!applied) {
            return new MealInfo(mealType, mealType.getLabel(), false, false,
                mealType.getLabel() + " 신청내역 없음. 교직원에게 문의하세요!");
        }

        return new MealInfo(mealType, mealType.getLabel(), true, false,
            mealType.getLabel() + " 태그를 확인해주세요.");
    }

    private TagResponse withMealInfo(TagResponse response, MealInfo mealInfo) {
        if (mealInfo == null) {
            return response;
        }
        return TagResponse.builder()
            .processed(response.processed())
            .action(response.action())
            .actionLabel(response.actionLabel())
            .studentId(response.studentId())
            .studentName(response.studentName())
            .studentNumber(response.studentNumber())
            .seatLabel(response.seatLabel())
            .checkInAt(response.checkInAt())
            .checkOutAt(response.checkOutAt())
            .studyTimeMinutes(response.studyTimeMinutes())
            .messages(response.messages())
            .pendingActions(response.pendingActions())
            .dsaSynced(response.dsaSynced())
            .mealInfo(mealInfo)
            .build();
    }

    private List<PendingAction> buildPendingActions(List<ApprovedRequest> requests) {
        List<PendingAction> actions = new ArrayList<>();
        for (ApprovedRequest req : requests) {
            String regGn = req.regGn();
            boolean isEarlyLeave = "6".equals(regGn)
                || "C".equalsIgnoreCase(regGn) || "조퇴".equals(regGn);

            // 외출만 30분 제한, 조퇴는 항상 허용
            if (!isEarlyLeave && !isWithin30Minutes(req.regDt())) {
                continue;
            }

            AttendAction action;
            String message;

            if (isEarlyLeave) {
                action = AttendAction.C;
                message = "조퇴 하시겠습니까?";
            } else if ("4".equals(regGn) || "7".equals(regGn)
                || "D".equalsIgnoreCase(regGn) || "N".equalsIgnoreCase(regGn)
                || "외출".equals(regGn)) {
                action = AttendAction.D;
                message = "외출 하시겠습니까?";
            } else {
                action = AttendAction.D;
                message = "승인된 신청이 있습니다. 외출 하시겠습니까?";
            }

            actions.add(new PendingAction(action, message, req.regCd()));
        }
        return actions;
    }

    // ── 액션 핸들러 ──

    private TagResponse handleReAttendAfterEarlyLeave(Student student, Store store,
                                                       Long storeId) {
        boolean reAttendSuccess = dsaAttendanceService.sendReAttendProc(
            student.getRfidUid(), store);

        if (!reAttendSuccess) {
            log.warn("DSA 재등원 처리 실패. studentId: {}, storeId: {}",
                student.getId(), storeId);
            throw new AttendanceException(ErrorCode.EARLY_LEAVE_RE_ATTEND_FAILED);
        }

        // DSA 재등원 성공 → 등원 처리 (좌석 IN_USE 복원)
        Attendance attendance = Attendance.builder()
            .student(student)
            .store(student.getStore())
            .checkInAt(LocalDateTime.now())
            .build();
        attendanceRepository.save(attendance);

        seatCheckIn(student, storeId);

        List<String> messages = studentMessageRepository
            .findAllActiveByStudentId(student.getId())
            .stream()
            .map(StudentMessage::getContent)
            .toList();

        return TagResponse.builder()
            .processed(true)
            .action(AttendAction.S)
            .actionLabel("재등원")
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .checkInAt(attendance.getCheckInAt())
            .messages(messages.isEmpty() ? null : messages)
            .dsaSynced(true)
            .build();
    }

    private TagResponse handleCheckIn(AttendAction action, Student student,
                                      Long storeId, boolean dsaSynced) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean alreadyCheckedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .isPresent();

        if (alreadyCheckedIn) {
            throw new AttendanceException(ErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.builder()
            .student(student)
            .store(student.getStore())
            .checkInAt(LocalDateTime.now())
            .build();
        attendanceRepository.save(attendance);

        seatCheckIn(student, storeId);

        List<String> messages = studentMessageRepository
            .findAllActiveByStudentId(student.getId())
            .stream()
            .map(StudentMessage::getContent)
            .toList();

        return TagResponse.builder()
            .processed(true)
            .action(action)
            .actionLabel(action.getLabel())
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .checkInAt(attendance.getCheckInAt())
            .messages(messages.isEmpty() ? null : messages)
            .dsaSynced(dsaSynced)
            .build();
    }

    private TagResponse handleCheckOut(AttendAction action, Student student,
                                       Long storeId, boolean dsaSynced) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Attendance attendance = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .orElseThrow(() -> new AttendanceException(ErrorCode.NOT_CHECKED_IN));

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.checkOut(checkOutTime);

        // 진행 중인 외출 자동 종료
        outingRepository.findActiveOutingByStudentToday(
                student.getId(), startOfDay, endOfDay)
            .ifPresent(outing -> {
                outing.endOuting(checkOutTime);
                studyTimeRedisService.addOutingDeduction(
                    storeId, student.getId(),
                    outing.getStartedAt(), outing.getEndedAt());
            });

        seatCheckOut(student, storeId);

        long totalMinutes = Duration.between(
            attendance.getCheckInAt(), checkOutTime).toMinutes();
        long deductionMinutes = studyTimeRedisService.getTotalDeduction(
            storeId, student.getId());
        long mealDeduction = calculateMealDeduction(
            student.getId(), attendance.getCheckInAt().toLocalTime(),
            checkOutTime.toLocalTime(), today);
        long studyTimeMinutes = Math.max(
            totalMinutes - deductionMinutes - mealDeduction, 0);

        return TagResponse.builder()
            .processed(true)
            .action(action)
            .actionLabel(action.getLabel())
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .checkInAt(attendance.getCheckInAt())
            .checkOutAt(checkOutTime)
            .studyTimeMinutes(studyTimeMinutes)
            .dsaSynced(dsaSynced)
            .build();
    }

    private TagResponse handleOutingStart(AttendAction action, Student student,
                                          Long storeId, boolean dsaSynced) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean isCheckedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .isPresent();

        if (!isCheckedIn) {
            throw new AttendanceException(ErrorCode.NOT_CHECKED_IN);
        }

        boolean alreadyOnOuting = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay)
            .isPresent();

        if (alreadyOnOuting) {
            throw new AttendanceException(ErrorCode.ALREADY_ON_OUTING);
        }

        Outing outing = Outing.builder()
            .student(student)
            .store(student.getStore())
            .startedAt(LocalDateTime.now())
            .build();
        outingRepository.save(outing);

        seatUsageRepository.findByStudentIdAndStatus(
                student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                seatRedisService.markSeatOuting(
                    storeId, usage.getSeat().getId(),
                    student.getId(), student.getName());
            });

        return TagResponse.builder()
            .processed(true)
            .action(action)
            .actionLabel(action.getLabel())
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .dsaSynced(dsaSynced)
            .build();
    }

    private TagResponse handleOutingEnd(AttendAction action, Student student,
                                        Long storeId, boolean dsaSynced) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Outing outing = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay)
            .orElseThrow(() -> new AttendanceException(ErrorCode.NOT_ON_OUTING));

        outing.endOuting(LocalDateTime.now());

        studyTimeRedisService.addOutingDeduction(
            storeId, student.getId(),
            outing.getStartedAt(), outing.getEndedAt());

        seatUsageRepository.findByStudentIdAndStatus(
                student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                seatRedisService.markSeatInUse(
                    storeId, usage.getSeat().getId(),
                    student.getId(), student.getName());
            });

        return TagResponse.builder()
            .processed(true)
            .action(action)
            .actionLabel(action.getLabel())
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .dsaSynced(dsaSynced)
            .build();
    }

    private TagResponse handleSeatLeaveEnd(SeatLeave seatLeave, Student student,
                                              Long storeId) {
        seatLeave.endLeave();

        studyTimeRedisService.addSeatLeaveDeduction(
            storeId, student.getId(),
            seatLeave.getStartedAt(), seatLeave.getEndedAt());

        if (seatLeave.getSeat() != null) {
            seatRedisService.markSeatInUse(
                storeId, seatLeave.getSeat().getId(),
                student.getId(), student.getName());
        }

        return TagResponse.builder()
            .processed(true)
            .action(AttendAction.R)
            .actionLabel("좌석 복귀")
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .dsaSynced(true)
            .build();
    }

    // ── 급식 차감 계산 ──

    private long calculateMealDeduction(Long studentId, LocalTime checkInTime,
                                         LocalTime checkOutTime, LocalDate today) {
        List<MealTag> mealTags = mealTagRepository.findAllByStudentIdAndMealDate(
            studentId, today);

        long totalDeduction = 0;
        for (MealTag tag : mealTags) {
            MealType type = tag.getMealType();
            LocalTime mealStart = type.getStartTime();
            LocalTime mealEnd = type.getEndTime();

            LocalTime overlapStart = checkInTime.isAfter(mealStart) ? checkInTime : mealStart;
            LocalTime overlapEnd = checkOutTime.isBefore(mealEnd) ? checkOutTime : mealEnd;

            if (overlapStart.isBefore(overlapEnd)) {
                totalDeduction += Duration.between(overlapStart, overlapEnd).toMinutes();
            }
        }
        return totalDeduction;
    }

    // ── 좌석 처리 ──

    private void seatCheckIn(Student student, Long storeId) {
        Seat seat = student.getAssignedSeat();
        if (seat == null) {
            return;
        }

        if (seatUsageRepository.findByStudentIdAndStatus(
                student.getId(), SeatUsageStatus.IN_USE).isPresent()) {
            return;
        }

        Long seatId = seat.getId();
        boolean acquired = seatRedisService.tryOccupySeat(
            storeId, seatId, student.getId(), student.getName());

        if (!acquired) {
            String existing = seatRedisService.getSeatStatus(storeId, seatId);
            if (existing != null
                    && existing.contains(":" + student.getId() + ":")) {
                log.info("[Tag] stale Redis 데이터 정리 후 재선점 (studentId={}, seatId={})",
                    student.getId(), seatId);
                seatRedisService.markSeatInUse(
                    storeId, seatId, student.getId(), student.getName());
            } else {
                log.warn("[Tag] 배정 좌석 선점 실패 (studentId={}, seatId={}, existing={})",
                    student.getId(), seatId, existing);
                return;
            }
        }

        try {
            SeatUsage seatUsage = SeatUsage.builder()
                .seat(seat)
                .student(student)
                .store(seat.getStore())
                .startedAt(LocalDateTime.now())
                .build();
            seatUsageRepository.save(seatUsage);
        } catch (Exception e) {
            seatRedisService.releaseSeat(storeId, seatId);
            log.warn("[Tag] 좌석 DB 저장 실패, Redis 롤백 (studentId={}, seatId={})",
                student.getId(), seatId, e);
        }
    }

    private void seatCheckOut(Student student, Long storeId) {
        seatUsageRepository.findByStudentIdAndStatus(
                student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> {
                usage.endUsage(LocalDateTime.now());
                try {
                    seatRedisService.releaseSeat(storeId, usage.getSeat().getId());
                } catch (Exception e) {
                    log.warn("[Tag] Redis 좌석 해제 실패 (seatId={})",
                        usage.getSeat().getId(), e);
                }
            });
    }

    private String getSeatLabel(Student student) {
        if (student.getAssignedSeat() != null) {
            return student.getAssignedSeat().getSeatLabel();
        }
        return null;
    }

    private void validateStudentStore(Student student, Long storeId) {
        if (!student.getStore().getId().equals(storeId)) {
            throw new KioskException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }
    }
}
