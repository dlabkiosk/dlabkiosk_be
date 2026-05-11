package com.moduletest.deasungkioskbackend.domain.tag.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAnomalyLogService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAttendanceService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAttendanceService.AttendTagResult;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaMealService;
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
import com.moduletest.deasungkioskbackend.domain.meal.service.MealUnappliedLogService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final DsaAnomalyLogService dsaAnomalyLogService;
    private final MealTagRepository mealTagRepository;
    private final MealUnappliedLogService mealUnappliedLogService;
    private final SeatLeaveRepository seatLeaveRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int RETAG_INTERVAL_SECONDS = 60;
    private static final String RETAG_KEY_PREFIX = "tag:recent:";

    @Transactional
    public TagResponse processTag(TagRequest request, Long storeId) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());
        validateStudentStore(student, storeId);
        // 비관적 락: 동일 학생 동시 태그 방지
        studentRepository.findByIdForUpdate(student.getId());

        // 재태그 간격 가드 — 같은 학생이 RETAG_INTERVAL_SECONDS 이내 재태그 시 차단
        // (RFID 더블리드/프론트 재시도로 phantom 외출/조퇴가 생기는 문제 방어)
        guardRetagInterval(student.getId());

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new KioskException(ErrorCode.STORE_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        MealType mealType = MealType.fromCurrentTime(LocalTime.now());
        MealInfo mealInfo = buildMealInfo(mealType, student, store, today);

        // 1. 외출 중이면 → DSA 응답 R 확인 후 복귀 처리 (+ 급식 정보 포함)
        Optional<Outing> activeOuting = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay);
        if (activeOuting.isPresent()) {
            AttendTagResult dsaResult = dsaAttendanceService.sendAttendTag(
                student.getRfidUid(), store);
            if (!dsaResult.dsaSynced() || dsaResult.action() != AttendAction.R) {
                log.warn("외출 복귀 실패 - dsaSynced: {}, action: {}. studentId: {}, storeId: {}",
                    dsaResult.dsaSynced(), dsaResult.action(), student.getId(), storeId);
                dsaAnomalyLogService.log(
                    storeId, student.getRfidUid(), "/kiosk/tag",
                    "RETURN_CONTEXT_NOT_R",
                    Map.of("studentId", student.getId(),
                        "studentName", student.getName(),
                        "studentNumber", student.getStudentNumber(),
                        "outingStartedAt", activeOuting.get().getStartedAt().toString(),
                        "dsaSynced", dsaResult.dsaSynced(),
                        "dsaAction", String.valueOf(dsaResult.action())),
                    dsaResult,
                    "외출 중 상태에서 복귀 태그인데 DSA가 R 아닌 응답 반환 — DSA-우리 DB 상태 불일치");
                throw new AttendanceException(ErrorCode.DSA_SYNC_FAILED);
            }
            TagResponse response = handleOutingEnd(
                AttendAction.R, student, storeId, true);
            recordMealUnappliedIfNeeded(student, store, mealType, mealInfo, today, "R");
            return withMealInfo(response, mealInfo);
        }

        // 2. 좌석이탈 중이면 → 복귀 처리 (+ 급식 정보 포함)
        Optional<SeatLeave> activeSeatLeave = seatLeaveRepository
            .findActiveSeatLeaveByStudentToday(student.getId(), startOfDay);
        if (activeSeatLeave.isPresent()) {
            TagResponse response = handleSeatLeaveEnd(
                activeSeatLeave.get(), student, storeId);
            recordMealUnappliedIfNeeded(student, store, mealType, mealInfo, today, "R");
            return withMealInfo(response, mealInfo);
        }

        // 3. 미등원이면 → 등원 처리 우선 (+ 급식 정보 포함)
        Optional<Attendance> checkedIn = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN);
        if (checkedIn.isEmpty()) {
            // 오늘 조퇴 기록 있으면 → DSA 3.22 재등원 처리 (DSA 응답 코드 의존 X)
            boolean hadEarlyLeaveToday = attendanceRepository.existsEarlyLeaveToday(
                student.getId(), startOfDay, endOfDay);
            if (hadEarlyLeaveToday) {
                TagResponse response = handleReAttendAfterEarlyLeave(
                    student, store, storeId);
                return withMealInfo(response, mealInfo);
            }

            AttendTagResult dsaResult = dsaAttendanceService.sendAttendTag(
                student.getRfidUid(), store);

            // DSA가 거부(이미 하원/학습시간 외 등) — 등원 row 만들지 않고 메시지 노출
            if (dsaResult.rejected()) {
                String rejectMsg = dsaResult.rejectMessage() != null
                    ? dsaResult.rejectMessage()
                    : "출결 처리할 수 없습니다. 데스크로 문의해주세요.";
                return TagResponse.builder()
                    .processed(true)
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .seatLabel(getSeatLabel(student))
                    .dsaSynced(true)
                    .messages(List.of(rejectMsg))
                    .mealInfo(mealInfo)
                    .build();
            }

            // 우리 DB 미등원이지만 DSA가 사유신청 선택지(code 126/128/129) 응답.
            // = DSA에는 등원중 + 사유신청 승인 있음 (수기등원 학생). prompts 노출하여 confirmTag 호출 유도.
            if (dsaResult.hasApprovalPrompts()) {
                List<PendingAction> pendingActions = dsaResult.approvalPrompts().stream()
                    .map(p -> new PendingAction(p.action(), p.message(), null))
                    .toList();
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

            // DSA가 자동 판별 못 함 (code 113) — 사용자가 하원/외출 직접 선택.
            // handleCheckedInTag와 동일하게 처리.
            if (dsaResult.userChoice()) {
                return buildFreeChoiceResponse(student, mealInfo);
            }

            if (!dsaResult.dsaSynced() || dsaResult.action() == null) {
                log.warn("DSA 출결 판별 실패 - 등원 처리 보류. studentId: {}, storeId: {}",
                    student.getId(), storeId);
                return TagResponse.builder()
                    .processed(false)
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .seatLabel(getSeatLabel(student))
                    .dsaSynced(false)
                    .messages(List.of("출결 처리 실패. 잠시 후 다시 시도해주세요."))
                    .mealInfo(mealInfo)
                    .build();
            }

            // 우리 DB에 등원 row 없는 상태에서 DSA가 알려준 액션대로 처리.
            // DSA가 S/A 외 응답을 줬다는 건 DSA만 등원중인 상태 (수기등원 등). 필요한 row 백필 후 처리.
            AttendAction action = dsaResult.action();
            TagResponse response = switch (action) {
                case S, A -> handleCheckIn(action, student, storeId, true);
                case T -> {
                    backfillAttendance(student, store);
                    yield handleCheckOut(AttendAction.T, student, storeId, true);
                }
                case C -> {
                    backfillAttendance(student, store);
                    yield handleCheckOut(AttendAction.C, student, storeId, true);
                }
                case D, N -> {
                    backfillAttendance(student, store);
                    yield handleOutingStart(action, student, storeId, true);
                }
                case R -> {
                    backfillAttendance(student, store);
                    backfillOuting(student, store);
                    yield handleOutingEnd(AttendAction.R, student, storeId, true);
                }
                default -> throw new AttendanceException(ErrorCode.DSA_SYNC_FAILED);
            };
            recordMealUnappliedIfNeeded(student, store, mealType, mealInfo, today, action.name());
            return withMealInfo(response, mealInfo);
        }

        // 4. 등원 상태 → DSA 3.14 응답 기반 처리. 식사시간/주말/공휴일/마감시간 모두 DSA가 판별.
        if (mealType != null) {
            recordMealUnappliedIfNeeded(student, store, mealType, mealInfo, today, null);
        }
        return handleCheckedInTag(student, store, storeId, mealInfo);
    }

    private TagResponse handleCheckedInTag(Student student, Store store, Long storeId,
                                           MealInfo mealInfo) {
        // con_gn="" 로 3.14 호출 → DSA가 사유신청 기반 선택지(126/128/129) 또는 액션(T/D/N/R/C) 반환
        AttendTagResult result = dsaAttendanceService.sendAttendTag(
            student.getRfidUid(), store);

        // DSA 사유신청 선택지 (조퇴/사유외출) — 사용자가 선택해서 confirmTag로 재호출
        if (result.hasApprovalPrompts()) {
            List<PendingAction> pendingActions = result.approvalPrompts().stream()
                .map(p -> new PendingAction(p.action(), p.message(), null))
                .toList();
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

        // 공휴일 등 DSA 시간표 없음 → 사용자가 하원/외출 선택
        if (result.userChoice()) {
            return buildFreeChoiceResponse(student, mealInfo);
        }

        if (result.rejected()) {
            // 식사시간이면 거부 메시지 숨기고 식사 안내만 노출 (학생이 식사 또는 자리 복귀)
            if (mealInfo != null) {
                return TagResponse.builder()
                    .processed(false)
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .seatLabel(getSeatLabel(student))
                    .dsaSynced(true)
                    .mealInfo(mealInfo)
                    .build();
            }
            String rejectMsg = result.rejectMessage() != null
                ? result.rejectMessage()
                : "사전조퇴/사전외출 승인 내역이 없습니다. 데스크로 문의해주세요.";
            return TagResponse.builder()
                .processed(true)
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentNumber())
                .seatLabel(getSeatLabel(student))
                .dsaSynced(true)
                .messages(List.of(rejectMsg))
                .build();
        }

        // DSA 판별 실패(noDsa) — action 못 받음 → 처리 보류 (이전엔 T로 강제 처리하던 버그)
        if (!result.dsaSynced() || result.action() == null) {
            log.warn("DSA 출결 판별 실패 - 처리 보류. studentId: {}, storeId: {}",
                student.getId(), storeId);
            // 식사시간이면 식사 안내만 노출 — 식사 흐름은 DSA 3.14 의존 안 함
            if (mealInfo != null) {
                return TagResponse.builder()
                    .processed(false)
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .seatLabel(getSeatLabel(student))
                    .dsaSynced(false)
                    .mealInfo(mealInfo)
                    .build();
            }
            return TagResponse.builder()
                .processed(false)
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentNumber())
                .seatLabel(getSeatLabel(student))
                .dsaSynced(false)
                .messages(List.of("출결 처리 실패. 잠시 후 다시 시도해주세요."))
                .build();
        }

        // DSA가 명시한 액션대로 처리.
        // R(복귀)인데 우리 DB에 외출 row 없으면 (수기 외출 케이스) Outing 백필 후 처리.
        TagResponse response = switch (result.action()) {
            case T -> handleCheckOut(AttendAction.T, student, storeId, true);
            case C -> handleCheckOut(AttendAction.C, student, storeId, true);
            case D, N -> handleOutingStart(result.action(), student, storeId, true);
            case R -> {
                backfillOutingIfMissing(student, store);
                yield handleOutingEnd(AttendAction.R, student, storeId, true);
            }
            default -> throw new AttendanceException(ErrorCode.DSA_SYNC_FAILED);
        };
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

        // action → con_gn 매핑. DSA가 con_gn 기준으로 사유신청 재검증하므로 우리 쪽 사전 검증 불필요.
        // T(하원)도 con_gn="T"로 명시해야 DSA가 자율등원일/공휴일에 113(userChoice) 재요청 없이 하원 처리함.
        String conGn = switch (request.action()) {
            case D -> "D";
            case N -> "N";
            case C -> "C";
            case T -> "T";
            default -> "";
        };

        // DSA 3.14에 con_gn 실어서 전송 — 실패/거부 시 처리 중단
        AttendTagResult confirmResult = dsaAttendanceService.sendAttendTag(
            student.getRfidUid(), store, conGn);
        if (confirmResult.rejected()) {
            throw new AttendanceException(ErrorCode.DSA_REJECTED);
        }
        if (!confirmResult.dsaSynced()) {
            throw new AttendanceException(ErrorCode.DSA_SYNC_FAILED);
        }

        // 외출/조퇴 확정 시 식사 미신청 재원 로그 삭제 (매장 떠난 거라 의미 없음)
        if (request.action() == AttendAction.D || request.action() == AttendAction.N
                || request.action() == AttendAction.C) {
            MealType mealType = MealType.fromCurrentTime(LocalTime.now());
            if (mealType != null) {
                mealUnappliedLogService.removeIfExists(
                    student.getId(), LocalDate.now(), mealType);
            }
        }

        // 수기등원 케이스 — 우리 DB에 등원 row 없으면 여기서 백필. 이후 핸들러가 정상 동작.
        backfillAttendanceIfMissing(student, store);

        return switch (request.action()) {
            case D, N -> handleOutingStart(
                request.action(), student, storeId, true);
            case C -> handleCheckOut(
                AttendAction.C, student, storeId, true);
            case T -> handleCheckOut(
                AttendAction.T, student, storeId, true);
            default -> throw new AttendanceException(ErrorCode.INVALID_INPUT_VALUE);
        };
    }

    private TagResponse buildFreeChoiceResponse(Student student, MealInfo mealInfo) {
        List<PendingAction> actions = List.of(
            new PendingAction(AttendAction.T, "하원 하시겠습니까?", null),
            new PendingAction(AttendAction.D, "외출 하시겠습니까?", null)
        );
        return TagResponse.builder()
            .processed(false)
            .studentId(student.getId())
            .studentName(student.getName())
            .studentNumber(student.getStudentNumber())
            .seatLabel(getSeatLabel(student))
            .pendingActions(actions)
            .dsaSynced(true)
            .mealInfo(mealInfo)
            .build();
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
                    mealType.getLabel() + " 신청내역 없음. 데스크로 문의하세요!"))
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

    private void recordMealUnappliedIfNeeded(Student student, Store store, MealType mealType,
        MealInfo mealInfo, LocalDate mealDate, String tagAction) {
        if (mealType == null || mealInfo == null) {
            return;
        }
        if (mealInfo.applied()) {
            return;
        }
        mealUnappliedLogService.recordIfUnapplied(student, store, mealType, mealDate, tagAction);
    }

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
                mealType.getLabel() + " 신청내역 없음. 데스크로 문의하세요!");
        }

        return new MealInfo(mealType, mealType.getLabel(), true, false,
            mealType.getLabel() + " 식사 신청 확인 완료. 태그해주세요.");
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

        // DSA 3.22 의미: 조퇴 → 외출 변환 + 복귀.
        // 우리 DB는 직전 조퇴 row를 CHECKED_IN으로 되돌려 등원시각 계승하고 조퇴 카운트를 0으로 만든다.
        // outing row는 만들지 않음 — DSA가 출결 source of truth고 우리 outing은 외출권 통제 도구라서
        // 실수 조퇴→재등원으로 외출권 차감하는 건 부적절.
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<Attendance> earlyLeaves = attendanceRepository.findEarlyLeaveToday(
            student.getId(), startOfDay, endOfDay);
        Attendance attendance = earlyLeaves.isEmpty() ? null : earlyLeaves.get(0);

        if (attendance != null) {
            attendance.cancelCheckOut();
        } else {
            // 방어: existsEarlyLeaveToday로 분기했지만 트랜잭션 사이 사라진 케이스
            attendance = Attendance.builder()
                .student(student)
                .store(student.getStore())
                .checkInAt(LocalDateTime.now())
                .build();
            attendanceRepository.save(attendance);
        }

        seatCheckIn(student, storeId);

        List<String> messages = studentMessageRepository
            .findActiveMessageContents(student.getId());

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
            .findActiveMessageContents(student.getId());

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
        attendance.checkOut(checkOutTime, action.name());

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

        // JPA AUTO flush — 방금 close한 attendance도 findCompletedTodayByStudentId에 포함됨.
        // currentMinutes를 따로 더하면 이중 계산되므로 한번에 합산.
        List<Attendance> completedToday = attendanceRepository
            .findCompletedTodayByStudentId(student.getId(), startOfDay, endOfDay);
        long totalMinutes = completedToday.stream()
            .mapToLong(a -> Duration.between(a.getCheckInAt(), a.getCheckOutAt()).toMinutes())
            .sum();
        long deductionMinutes = studyTimeRedisService.getOutingDeduction(
            storeId, student.getId());
        long mealDeduction = calculateMealDeduction(
            student.getId(), today, completedToday);
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

    /**
     * 급식 태그한 식사 시간대와 학생의 등원 구간이 겹치는 만큼만 차감.
     * 예: 점심 12:10~13:10에 12:50 조퇴면 40분만 차감.
     * 다회 등원이면 각 등원 구간의 overlap을 모두 합산.
     */
    private long calculateMealDeduction(Long studentId, LocalDate today,
                                         List<Attendance> attendances) {
        List<MealTag> mealTags = mealTagRepository.findAllByStudentIdAndMealDate(
            studentId, today);

        long totalDeduction = 0;
        for (MealTag tag : mealTags) {
            MealType type = tag.getMealType();
            LocalDateTime mealStart = LocalDateTime.of(today, type.getStartTime());
            LocalDateTime mealEnd = LocalDateTime.of(today, type.getEndTime());

            for (Attendance att : attendances) {
                if (att.getCheckOutAt() == null) {
                    continue;
                }
                LocalDateTime overlapStart = att.getCheckInAt().isAfter(mealStart)
                    ? att.getCheckInAt() : mealStart;
                LocalDateTime overlapEnd = att.getCheckOutAt().isBefore(mealEnd)
                    ? att.getCheckOutAt() : mealEnd;
                if (overlapEnd.isAfter(overlapStart)) {
                    totalDeduction += Duration.between(overlapStart, overlapEnd).toMinutes();
                }
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
            log.error("[Tag] 좌석 DB 저장 실패, Redis 롤백 (studentId={}, seatId={})",
                student.getId(), seatId, e);
            throw e;
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

    // ── 백필 헬퍼 ──
    // 수기등원/수기외출처럼 DSA에만 상태가 있고 우리 DB엔 row가 없는 케이스 대응.
    // checkInAt/startedAt은 현재시각으로 박힘. 어드민이 PUT /admin/attendances/check-in-time으로 보정 가능.

    private void backfillAttendance(Student student, Store store) {
        Attendance attendance = Attendance.builder()
            .student(student)
            .store(store)
            .checkInAt(LocalDateTime.now())
            .build();
        attendanceRepository.save(attendance);
        log.info("Attendance 백필 (수기등원 대응). studentId: {}, storeId: {}",
            student.getId(), store.getId());
    }

    private void backfillAttendanceIfMissing(Student student, Store store) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean exists = attendanceRepository
            .findTodayAttendanceByStudentAndStatus(
                student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN)
            .isPresent();
        if (!exists) {
            backfillAttendance(student, store);
        }
    }

    private void backfillOuting(Student student, Store store) {
        Outing outing = Outing.builder()
            .student(student)
            .store(store)
            .startedAt(LocalDateTime.now())
            .build();
        outingRepository.save(outing);
        log.info("Outing 백필 (수기외출 대응). studentId: {}, storeId: {}",
            student.getId(), store.getId());
    }

    private void backfillOutingIfMissing(Student student, Store store) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean exists = outingRepository
            .findActiveOutingByStudentToday(student.getId(), startOfDay, endOfDay)
            .isPresent();
        if (!exists) {
            backfillOuting(student, store);
        }
    }

    /**
     * 재태그 간격 가드. 같은 학생이 RETAG_INTERVAL_SECONDS 이내 태그하면
     * TAG_TOO_SOON 예외로 차단. setIfAbsent로 원자적 락 처리.
     */
    private void guardRetagInterval(Long studentId) {
        String key = RETAG_KEY_PREFIX + studentId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
            key, "1", Duration.ofSeconds(RETAG_INTERVAL_SECONDS));
        if (!Boolean.TRUE.equals(acquired)) {
            log.info("재태그 간격 미만 - 차단. studentId: {}, interval: {}s",
                studentId, RETAG_INTERVAL_SECONDS);
            throw new KioskException(ErrorCode.TAG_TOO_SOON);
        }
    }
}
