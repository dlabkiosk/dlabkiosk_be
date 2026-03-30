package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatType;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeave;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final StoreRepository storeRepository;
    private final SeatRedisService seatRedisService;
    private final SeatLeaveRepository seatLeaveRepository;
    private final StudentRepository studentRepository;
    private final DsaAreaService dsaAreaService;


    @Transactional
    public List<SeatStatusResponse> findSeatStatusByArea(Long storeId, String areaCd) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<SeatStatusResponse> dsaSeats = dsaAreaService.findSeatStatusByArea(areaCd, store);

        // Redis AWAY 상태 + seatCd 기준 매핑
        Map<Object, Object> redisStatus = seatRedisService.getSeatStatusMap(storeId);
        List<Seat> seats = seatRepository.findAllByStoreIdWithStore(storeId);

        Map<String, Seat> seatCdMap = new HashMap<>();
        Map<String, Boolean> awaySeatCds = new HashMap<>();
        for (Seat seat : seats) {
            if (seat.getSeatCd() != null) {
                seatCdMap.put(seat.getSeatCd(), seat);
            }
            String status = (String) redisStatus.get(seat.getId().toString());
            if (status != null && status.startsWith("AWAY:") && seat.getSeatCd() != null) {
                awaySeatCds.put(seat.getSeatCd(), true);
            }
        }

        // 활성 이탈 건에서 좌석ID → 사유명 매핑
        List<SeatLeave> activeLeaves = seatLeaveRepository.findActiveByStoreId(
            storeId, LocalDate.now().atStartOfDay());
        Map<Long, String> seatIdToReasonName = new HashMap<>();
        for (SeatLeave leave : activeLeaves) {
            seatIdToReasonName.put(leave.getSeat().getId(),
                leave.getReason().getReasonName());
        }

        // DSA 좌석 → 우리 DB upsert (seatCd 기준 매칭)
        String areaNm = findAreaName(areaCd, store);
        for (SeatStatusResponse dsaSeat : dsaSeats) {
            if (dsaSeat.seatNm() == null || dsaSeat.seatNm().isEmpty()) {
                continue;
            }
            Seat seat = seatCdMap.get(dsaSeat.seatCd());
            if (seat != null) {
                seat.syncFromDsa(dsaSeat.seatNm(), dsaSeat.seatCd(),
                    dsaSeat.xPos(), dsaSeat.yPos(),
                    areaCd, areaNm, dsaSeat.seatGn());
            } else {
                seat = seatRepository.save(Seat.builder()
                    .store(store)
                    .seatLabel(dsaSeat.seatNm())
                    .seatType(SeatType.INDIVIDUAL)
                    .xPos(dsaSeat.xPos())
                    .yPos(dsaSeat.yPos())
                    .active(true)
                    .areaCd(areaCd)
                    .areaNm(areaNm)
                    .seatCd(dsaSeat.seatCd())
                    .seatGn(dsaSeat.seatGn())
                    .dsaSynced(true)
                    .build());
                seatCdMap.put(seat.getSeatCd(), seat);
            }
        }

        // seatCd → 학생명 매핑 (upsert 뒤에 수행해야 seatCd 반영됨)
        List<Student> students = studentRepository.findAllByStoreIdWithStore(storeId);
        Map<String, String> seatCdToStudentName = new HashMap<>();
        for (Student student : students) {
            if (student.getAssignedSeat() != null
                && student.getAssignedSeat().getSeatCd() != null) {
                seatCdToStudentName.put(
                    student.getAssignedSeat().getSeatCd(), student.getName());
            }
        }

        // DSA 좌석에 학생명 + AWAY 상태/사유 덮어씌우기
        return dsaSeats.stream()
            .map(s -> {
                String studentName = seatCdToStudentName.get(s.seatCd());
                boolean away = awaySeatCds.containsKey(s.seatCd());
                if (away) {
                    Seat seat = seatCdMap.get(s.seatCd());
                    String reasonName = seat != null
                        ? seatIdToReasonName.get(seat.getId()) : null;
                    return new SeatStatusResponse(
                        s.seatCd(), s.seatNm(), s.xPos(), s.yPos(),
                        s.seatGn(), "A", true, studentName, reasonName);
                }
                return new SeatStatusResponse(
                    s.seatCd(), s.seatNm(), s.xPos(), s.yPos(),
                    s.seatGn(), s.state(), false, studentName, null);
            })
            .toList();
    }

    public List<AreaResponse> findAreasByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
        return dsaAreaService.findAreas(store);
    }

    // ===== 관리자 API =====

    public List<SeatResponse> findAllSeats(Long storeId, String areaCd) {
        List<Seat> seats;
        if (storeId != null) {
            seats = seatRepository.findAllByStoreIdWithStore(storeId);
        } else {
            seats = seatRepository.findAllWithStore();
        }

        return seats.stream()
            .filter(seat -> areaCd == null || areaCd.equals(seat.getAreaCd()))
            .map(SeatResponse::fromEntity)
            .toList();
    }

    public SeatResponse findSeatById(Long seatId) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        validateStoreAccess(seat);
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public SeatResponse createSeat(SeatCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        Seat seat = Seat.builder()
            .store(store)
            .seatLabel(request.seatLabel())
            .seatType(SeatType.valueOf(request.seatType()))
            .xPos(request.xPos())
            .yPos(request.yPos())
            .active(true)
            .build();

        Seat savedSeat = seatRepository.save(seat);
        return SeatResponse.fromEntity(savedSeat);
    }

    @Transactional
    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        validateStoreAccess(seat);

        seat.updateInfo(
            request.seatLabel(),
            SeatType.valueOf(request.seatType()),
            request.xPos(),
            request.yPos(),
            request.active()
        );
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
        validateStoreAccess(seat);
        seatRepository.delete(seat);
    }

    /**
     * 전 구역 좌석 동기화. DSA 3.7(구역) + 3.8(좌석) 호출하여 upsert.
     */
    @Transactional
    public void synchronizeSeats(Store store) {
        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        if (areas.isEmpty()) {
            return;
        }

        List<Seat> allSeats = seatRepository.findAllByStoreIdWithStore(store.getId());
        Map<String, Seat> seatCdMap = new HashMap<>();
        for (Seat seat : allSeats) {
            if (seat.getSeatCd() != null) {
                seatCdMap.put(seat.getSeatCd(), seat);
            }
        }

        for (AreaResponse area : areas) {
            List<SeatStatusResponse> dsaSeats =
                dsaAreaService.findSeatStatusByArea(area.areaCd(), store);

            for (SeatStatusResponse dsaSeat : dsaSeats) {
                if (dsaSeat.seatNm() == null || dsaSeat.seatNm().isEmpty()) {
                    continue;
                }
                Seat seat = seatCdMap.get(dsaSeat.seatCd());
                if (seat != null) {
                    seat.syncFromDsa(dsaSeat.seatNm(), dsaSeat.seatCd(),
                        dsaSeat.xPos(), dsaSeat.yPos(),
                        area.areaCd(), area.areaNm(), dsaSeat.seatGn());
                } else {
                    seat = seatRepository.save(Seat.builder()
                        .store(store)
                        .seatLabel(dsaSeat.seatNm())
                        .seatType(SeatType.INDIVIDUAL)
                        .xPos(dsaSeat.xPos())
                        .yPos(dsaSeat.yPos())
                        .active(true)
                        .areaCd(area.areaCd())
                        .areaNm(area.areaNm())
                        .seatCd(dsaSeat.seatCd())
                        .seatGn(dsaSeat.seatGn())
                        .dsaSynced(true)
                        .build());
                    seatCdMap.put(seat.getSeatCd(), seat);
                }
            }
        }
    }

    private String findAreaName(String areaCd, Store store) {
        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        for (AreaResponse area : areas) {
            if (area.areaCd().equals(areaCd)) {
                return area.areaNm();
            }
        }
        return null;
    }

    private void validateStoreAccess(Seat seat) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!seat.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

}
