package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.tag.entity.AttendAction;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DsaAttendanceService {

    private static final String SET_ATTEND_STD_PATH = "/kiosk/setAttendStd";
    private static final String SET_RE_ATTEND_PROC_PATH = "/kiosk/setReAttendProc";
    private static final int CODE_ALREADY_EARLY_LEFT = 121;
    private static final int CODE_CONFIRM_EARLY_LEAVE = 128;
    private static final int CODE_CONFIRM_OUTING = 129;
    private static final int CODE_NO_APPROVAL = 130;
    private static final DateTimeFormatter TAG_DT_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DsaApiClient dsaApiClient;
    private final DsaAnomalyLogService dsaAnomalyLogService;

    /**
     * DSA 3.14 setAttendStd 호출.
     * DSA가 시간 기준으로 att_gn(S/T/A/D/C/R)을 자동 판별하여 응답한다.
     *
     * @return AttendTagResult (action + earlyLeftBlocked 플래그)
     */
    public AttendTagResult sendAttendTag(String rfidUid, Store store) {
        return sendAttendTag(rfidUid, store, "");
    }

    public AttendTagResult sendAttendTag(String rfidUid, Store store, String conGn) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 로컬 판별 사용. storeId: {}", store.getId());
            return AttendTagResult.noDsa();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("tag_dt", LocalDateTime.now().format(TAG_DT_FORMAT));
            params.put("con_gn", conGn);

            DsaResponse response = dsaApiClient.post(
                SET_ATTEND_STD_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                if (response.getCode() == CODE_ALREADY_EARLY_LEFT) {
                    log.info("DSA 조퇴 후 재태그 감지 (code 121). rfidUid: {}, storeId: {}",
                        rfidUid, store.getId());
                    dsaAnomalyLogService.log(
                        store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                        "DSA_ALREADY_EARLY_LEFT", params, response,
                        "DSA code 121 — DSA 쪽은 이미 조퇴 처리됨 (우리 DB와 상태 불일치 가능)");
                    return AttendTagResult.blockedByEarlyLeave();
                }
                if (response.getCode() == CODE_NO_APPROVAL) {
                    log.info("DSA 승인 내역 없음 (code 130). rfidUid: {}, storeId: {}",
                        rfidUid, store.getId());
                    dsaAnomalyLogService.log(
                        store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                        "DSA_NO_APPROVAL", params, response,
                        "DSA code 130 — 승인 내역 없음 (top-level)");
                    return AttendTagResult.rejected(response.getMessage());
                }
                log.warn("DSA setAttendStd 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                dsaAnomalyLogService.log(
                    store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                    "DSA_UNKNOWN_FAILURE", params, response,
                    "DSA 실패 응답 (알 수 없는 code): " + response.getCode());
                return AttendTagResult.noDsa();
            }

            // data 내부 code 확인 (최상위 code=0이어도 내부에 거부 코드가 올 수 있음)
            Integer innerCode = extractInnerCode(response);
            if (innerCode != null && innerCode == CODE_NO_APPROVAL) {
                String innerMessage = extractInnerMessage(response);
                log.info("DSA 승인 내역 없음 (data 내부 code 130). rfidUid: {}, storeId: {}",
                    rfidUid, store.getId());
                dsaAnomalyLogService.log(
                    store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                    "DSA_NO_APPROVAL", params, response,
                    "DSA data 내부 code 130 — 승인 내역 없음");
                return AttendTagResult.rejected(innerMessage);
            }

            // data 내부 code 128/129 — DSA 확인 프롬프트 (조퇴/사유외출 신청하시겠습니까?)
            // 복귀 맥락에서 R 대신 이게 오면 DSA가 복귀를 인지 못하는 상태 불일치
            if (innerCode != null
                    && (innerCode == CODE_CONFIRM_EARLY_LEAVE || innerCode == CODE_CONFIRM_OUTING)) {
                String innerMessage = extractInnerMessage(response);
                log.info("DSA 확인 프롬프트 응답 (code {}): {}. rfidUid: {}, storeId: {}",
                    innerCode, innerMessage, rfidUid, store.getId());
                dsaAnomalyLogService.log(
                    store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                    "DSA_CONFIRM_PROMPT", params, response,
                    "DSA 확인 프롬프트 (code " + innerCode + "): " + innerMessage
                        + " — 복귀 맥락이면 DSA가 외출 상태 인지 못함");
                return AttendTagResult.noDsa();
            }

            String attGn = extractAttGn(response);
            if (attGn == null) {
                if (conGn != null && !conGn.isEmpty()) {
                    log.info("DSA 성공 (att_gn 없음, con_gn={} 지정). rfidUid: {}, storeId: {}",
                        conGn, rfidUid, store.getId());
                    return AttendTagResult.success(AttendAction.fromCode(conGn));
                }
                log.warn("DSA 응답에 att_gn 없음. storeId: {}, raw: {}",
                    store.getId(), response);
                dsaAnomalyLogService.log(
                    store.getId(), rfidUid, SET_ATTEND_STD_PATH,
                    "ATT_GN_MISSING", params, response,
                    "DSA 응답이 success인데 att_gn 필드 없음 → 처리 보류");
                return AttendTagResult.noDsa();
            }

            AttendAction action = AttendAction.fromCode(attGn);
            log.info("DSA 출결 판별: {} ({}). rfidUid: {}, storeId: {}",
                action.name(), action.getLabel(), rfidUid, store.getId());

            return AttendTagResult.success(action);

        } catch (DsaApiException e) {
            log.error("DSA setAttendStd 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return AttendTagResult.noDsa();
        } catch (Exception e) {
            log.error("DSA setAttendStd 예외. storeId: {}", store.getId(), e);
            return AttendTagResult.noDsa();
        }
    }

    /**
     * DSA 3.22 setReAttendProc 호출.
     * 조퇴 상태를 해제하고 재등원 처리한다 (조퇴→외출+복귀).
     *
     * @return 성공 여부
     */
    public boolean sendReAttendProc(String rfidUid, Store store) {
        if (!store.hasDsaCredentials()) {
            return false;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);

            DsaResponse response = dsaApiClient.post(
                SET_RE_ATTEND_PROC_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA setReAttendProc 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return false;
            }

            log.info("DSA 재등원 처리 성공. rfidUid: {}, storeId: {}",
                rfidUid, store.getId());
            return true;

        } catch (Exception e) {
            log.error("DSA setReAttendProc 예외. storeId: {}", store.getId(), e);
            return false;
        }
    }

    public record AttendTagResult(AttendAction action, boolean dsaSynced,
                                   boolean earlyLeftBlocked, boolean rejected,
                                   String rejectMessage) {

        public static AttendTagResult success(AttendAction action) {
            return new AttendTagResult(action, true, false, false, null);
        }

        public static AttendTagResult blockedByEarlyLeave() {
            return new AttendTagResult(null, true, true, false, null);
        }

        public static AttendTagResult rejected(String message) {
            return new AttendTagResult(null, true, false, true, message);
        }

        public static AttendTagResult noDsa() {
            return new AttendTagResult(null, false, false, false, null);
        }
    }

    /**
     * data가 이중 배열([[{...}], {...}])일 때 첫 번째 내부 리스트의 첫 Map을 꺼낸다.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractFirstDataMap(DsaResponse response) {
        Object data = response.getData();
        if (!(data instanceof List<?> outerList) || outerList.isEmpty()) {
            return null;
        }
        Object first = outerList.get(0);
        // 이중 배열: [[{...}]]
        if (first instanceof List<?> innerList && !innerList.isEmpty()
                && innerList.get(0) instanceof Map) {
            return (Map<String, Object>) innerList.get(0);
        }
        // 단일 배열: [{...}]
        if (first instanceof Map) {
            return (Map<String, Object>) first;
        }
        return null;
    }

    private Integer extractInnerCode(DsaResponse response) {
        Map<String, Object> dataMap = extractFirstDataMap(response);
        if (dataMap == null) {
            return null;
        }
        Object code = dataMap.get("code");
        if (code instanceof Number num) {
            return num.intValue();
        }
        return null;
    }

    private String extractInnerMessage(DsaResponse response) {
        Map<String, Object> dataMap = extractFirstDataMap(response);
        if (dataMap == null) {
            return null;
        }
        Object msg = dataMap.get("message");
        if (msg != null) {
            return msg.toString()
                .replace("\n", " ")
                .replace("선생님께 문의해주세요", "데스크로 문의해주세요");
        }
        return null;
    }

    private String extractAttGn(DsaResponse response) {
        // extra 필드에서 먼저 확인
        if (response.getExtra() != null) {
            Object attGn = response.getExtra().get("att_gn");
            if (attGn != null) {
                return attGn.toString().trim();
            }
        }

        // data에서 확인
        Map<String, Object> dataMap = extractFirstDataMap(response);
        if (dataMap != null) {
            Object attGn = dataMap.get("att_gn");
            if (attGn != null) {
                return attGn.toString().trim();
            }
        }

        return null;
    }
}
