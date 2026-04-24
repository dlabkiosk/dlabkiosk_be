package com.moduletest.deasungkioskbackend.domain.seatchangerequest.service;

import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaSeatService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatType;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.AreaAvailableSeatResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.AvailableSeatResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatWaitingStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequestStatus;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.exception.SeatChangeRequestException;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository.SeatChangeRequestRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatChangeRequestService {

    private final SeatChangeRequestRepository seatChangeRequestRepository;
    private final SeatRepository seatRepository;
    private final StudentRepository studentRepository;
    private final StudentResolverService studentResolverService;
    private final SeatRedisService seatRedisService;
    private final SeatUsageRepository seatUsageRepository;
    private final DsaAreaService dsaAreaService;
    private final DsaSeatService dsaSeatService;
    private final StoreRepository storeRepository;

    @Transactional
    public SeatChangeRequestResponse createRequest(SeatChangeRequestCreateRequest request,
                                                   Long storeId) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId, request.inputMethod());

        if (!student.getStore().getId().equals(storeId)) {
            throw new SeatChangeRequestException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }

        Seat desiredSeat1 = findSeatByCdInStore(request.desiredSeatCd1(), storeId);
        Seat desiredSeat2 = request.desiredSeatCd2() != null && !request.desiredSeatCd2().isBlank()
            ? findSeatByCdInStore(request.desiredSeatCd2(), storeId) : null;
        Seat desiredSeat3 = request.desiredSeatCd3() != null && !request.desiredSeatCd3().isBlank()
            ? findSeatByCdInStore(request.desiredSeatCd3(), storeId) : null;

        validateNotCurrentSeat(student, desiredSeat1, desiredSeat2, desiredSeat3);

        Optional<SeatChangeRequest> existingRequest =
            seatChangeRequestRepository.findByStudentIdAndStatusWithDetails(
                student.getId(), SeatChangeRequestStatus.PENDING);

        SeatChangeRequest saved;
        if (existingRequest.isPresent()) {
            SeatChangeRequest existing = existingRequest.get();
            existing.updateDesiredSeats(desiredSeat1, desiredSeat2, desiredSeat3);
            saved = existing;
            log.info("좌석 변경 신청 수정: studentId={}, 1순위={}, 2순위={}, 3순위={}",
                student.getId(),
                desiredSeat1.getSeatLabel(),
                desiredSeat2 != null ? desiredSeat2.getSeatLabel() : "없음",
                desiredSeat3 != null ? desiredSeat3.getSeatLabel() : "없음");
        } else {
            SeatChangeRequest seatChangeRequest = SeatChangeRequest.builder()
                .student(student)
                .store(student.getStore())
                .currentSeat(student.getAssignedSeat())
                .desiredSeat1(desiredSeat1)
                .desiredSeat2(desiredSeat2)
                .desiredSeat3(desiredSeat3)
                .build();
            saved = seatChangeRequestRepository.save(seatChangeRequest);
            log.info("좌석 변경 신청: studentId={}, 1순위={}, 2순위={}, 3순위={}",
                student.getId(),
                desiredSeat1.getSeatLabel(),
                desiredSeat2 != null ? desiredSeat2.getSeatLabel() : "없음",
                desiredSeat3 != null ? desiredSeat3.getSeatLabel() : "없음");
        }

        return SeatChangeRequestResponse.fromEntity(saved);
    }

    public SeatChangeRequestResponse findMyPendingRequest(String identifier, Long storeId,
                                                          InputMethod inputMethod) {
        Student student = studentResolverService.resolveAuto(
            identifier, storeId, inputMethod);
        return seatChangeRequestRepository
            .findByStudentIdAndStatusWithDetails(student.getId(), SeatChangeRequestStatus.PENDING)
            .map(SeatChangeRequestResponse::fromEntity)
            .orElse(null);
    }

    @Transactional
    public List<AreaAvailableSeatResponse> findAvailableSeatsByArea(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        List<Seat> allSeats = seatRepository.findAllByStoreIdWithStore(storeId);

        Map<String, Seat> seatCdMap = new HashMap<>();
        for (Seat seat : allSeats) {
            if (seat.getSeatCd() != null) {
                seatCdMap.put(seat.getSeatCd(), seat);
            }
        }

        List<AreaAvailableSeatResponse> result = new ArrayList<>();

        for (AreaResponse area : areas) {
            List<SeatStatusResponse> dsaSeats =
                dsaAreaService.findSeatStatusByArea(area.areaCd(), store);

            List<AreaAvailableSeatResponse.SeatInfo> seatInfos = new ArrayList<>();
            for (SeatStatusResponse dsaSeat : dsaSeats) {
                boolean saveToDB = dsaSeat.seatNm() != null
                    && !dsaSeat.seatNm().isEmpty();

                if (saveToDB) {
                    Seat seat = seatCdMap.get(dsaSeat.seatCd());
                    if (seat == null) {
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
                        log.info("DSA 좌석 자동 생성: seatCd={}, seatLabel={}, areaCd={}",
                            seat.getSeatCd(), seat.getSeatLabel(), area.areaCd());
                    } else {
                        seat.syncFromDsa(dsaSeat.seatNm(), dsaSeat.seatCd(),
                            dsaSeat.xPos(), dsaSeat.yPos(),
                            area.areaCd(), area.areaNm(), dsaSeat.seatGn());
                    }

                    seatInfos.add(AreaAvailableSeatResponse.SeatInfo.of(seat, dsaSeat));
                } else {
                    seatInfos.add(new AreaAvailableSeatResponse.SeatInfo(
                        null, dsaSeat.seatCd(), dsaSeat.seatNm(),
                        dsaSeat.xPos(), dsaSeat.yPos(), dsaSeat.seatGn()));
                }
            }

            result.add(new AreaAvailableSeatResponse(
                area.areaCd(), area.areaNm(), seatInfos));
        }

        return result;
    }

    public List<AvailableSeatResponse> findAvailableSeats(Long storeId) {
        List<Seat> allSeats = seatRepository.findAllByStoreIdWithStore(storeId);
        List<Student> allStudents = studentRepository.findAllByStoreIdAndActive(storeId, true);

        Map<Long, String> seatIdToStudentName = allStudents.stream()
            .filter(s -> s.getAssignedSeat() != null)
            .collect(Collectors.toMap(
                s -> s.getAssignedSeat().getId(),
                Student::getName,
                (a, b) -> a
            ));

        return allSeats.stream()
            .filter(Seat::isActive)
            .map(seat -> AvailableSeatResponse.of(
                seat, seatIdToStudentName.get(seat.getId())))
            .toList();
    }

    public Page<SeatChangeRequestResponse> findAllRequests(Long storeId,
                                                           SeatChangeRequestStatus status,
                                                           Pageable pageable) {
        if (storeId != null) {
            return seatChangeRequestRepository
                .findAllByStoreIdAndStatusWithDetails(storeId, status, pageable)
                .map(SeatChangeRequestResponse::fromEntity);
        }
        return seatChangeRequestRepository
            .findAllByStatusWithDetails(status, pageable)
            .map(SeatChangeRequestResponse::fromEntity);
    }

    public SeatChangeRequestResponse findRequestById(Long requestId) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));
        validateStoreAccess(request);
        return SeatChangeRequestResponse.fromEntity(request);
    }

    @Transactional
    public SeatChangeRequestResponse approveRequest(Long requestId, String seatCd) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));
        validateStoreAccess(request);

        if (request.getStatus() != SeatChangeRequestStatus.PENDING) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_CHANGE_ALREADY_PROCESSED);
        }

        int priority = getPriorityForSeatCd(request, seatCd);
        if (priority == 0) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_NOT_IN_REQUEST);
        }

        Seat approvedSeat = getSeatByPriority(request, priority);

        Student student = studentRepository.findByIdForUpdate(request.getStudent().getId())
            .orElseThrow(() -> new SeatChangeRequestException(ErrorCode.STUDENT_NOT_FOUND));

        if (studentRepository.existsByAssignedSeatIdAndIdNot(approvedSeat.getId(), student.getId())) {
            throw new SeatChangeRequestException(ErrorCode.DESIRED_SEAT_ALREADY_ASSIGNED);
        }

        // DSA 3.23 좌석 변경 동기화 (DSA 먼저, 실패 시 승인 중단)
        Store store = request.getStore();
        if (approvedSeat.getSeatCd() == null) {
            throw new BusinessException(ErrorCode.DSA_SEAT_CHANGE_FAILED);
        }
        boolean dsaSuccess = dsaSeatService.sendSeatChange(
            student.getRfidUid(), approvedSeat.getSeatCd(), store);
        if (!dsaSuccess) {
            throw new BusinessException(ErrorCode.DSA_SEAT_CHANGE_FAILED);
        }

        Seat oldSeat = student.getAssignedSeat();
        student.assignSeat(approvedSeat);
        handleRedisOnSeatChange(student, oldSeat, approvedSeat);

        // 등원 중이면 SeatUsage 좌석도 갱신
        seatUsageRepository.findByStudentIdAndStatus(
                student.getId(), SeatUsageStatus.IN_USE)
            .ifPresent(usage -> usage.changeSeat(approvedSeat));

        if (request.hasRemainingPriorities(priority)) {
            request.clearPrioritiesAtAndBelow(priority, approvedSeat);
            log.info("좌석 변경 부분 승인: requestId={}, studentId={}, {}순위 {} 배정, 상위 순위 대기 유지",
                requestId, student.getId(), priority, approvedSeat.getSeatLabel());
        } else {
            request.approve(approvedSeat);
            log.info("좌석 변경 승인: requestId={}, studentId={}, {} -> {}",
                requestId, student.getId(),
                oldSeat != null ? oldSeat.getSeatLabel() : "없음",
                approvedSeat.getSeatLabel());
        }

        return SeatChangeRequestResponse.fromEntity(request);
    }

    @Transactional
    public SeatChangeRequestResponse rejectRequest(Long requestId) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));
        validateStoreAccess(request);

        if (request.getStatus() != SeatChangeRequestStatus.PENDING) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_CHANGE_ALREADY_PROCESSED);
        }

        request.reject();

        log.info("좌석 변경 거절: requestId={}, studentId={}",
            requestId, request.getStudent().getId());

        return SeatChangeRequestResponse.fromEntity(request);
    }

    @Transactional
    public void cancelRequest(Long requestId, Long studentId) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));

        if (!request.getStudent().getId().equals(studentId)) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND);
        }

        if (request.getStatus() != SeatChangeRequestStatus.PENDING) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_CHANGE_ALREADY_PROCESSED);
        }

        seatChangeRequestRepository.delete(request);

        log.info("좌석 변경 신청 취소: requestId={}, studentId={}", requestId, studentId);
    }

    public List<SeatWaitingStatusResponse> findSeatWaitingStatus(Long storeId) {
        List<Seat> allSeats = seatRepository.findAllByStoreIdWithStore(storeId);
        List<SeatChangeRequest> pendingRequests =
            seatChangeRequestRepository.findAllByStoreIdAndStatusWithStudent(
                storeId, SeatChangeRequestStatus.PENDING);

        Map<Long, String> seatIdToStudentName =
            studentRepository.findAllWithAssignedSeatByStoreId(storeId).stream()
                .collect(Collectors.toMap(
                    s -> s.getAssignedSeat().getId(),
                    Student::getName,
                    (a, b) -> a
                ));

        Map<Long, List<SeatWaitingStatusResponse.WaitingStudent>> waitingBySeatId =
            buildWaitingBySeatId(pendingRequests);

        return allSeats.stream()
            .filter(Seat::isActive)
            .map(seat -> {
                List<SeatWaitingStatusResponse.WaitingStudent> waitingList =
                    waitingBySeatId.getOrDefault(seat.getId(), List.of());
                return new SeatWaitingStatusResponse(
                    seat.getId(),
                    seat.getSeatCd(),
                    seat.getSeatLabel(),
                    seat.getSeatType().name(),
                    seatIdToStudentName.get(seat.getId()),
                    waitingList.size(),
                    waitingList
                );
            })
            .toList();
    }

    private Map<Long, List<SeatWaitingStatusResponse.WaitingStudent>> buildWaitingBySeatId(
            List<SeatChangeRequest> pendingRequests) {
        Map<Long, List<SeatWaitingStatusResponse.WaitingStudent>> result = new HashMap<>();
        for (SeatChangeRequest req : pendingRequests) {
            addWaitingEntry(result, req, req.getDesiredSeat1(), 1);
            addWaitingEntry(result, req, req.getDesiredSeat2(), 2);
            addWaitingEntry(result, req, req.getDesiredSeat3(), 3);
        }
        for (List<SeatWaitingStatusResponse.WaitingStudent> list : result.values()) {
            list.sort((a, b) -> a.createdAt().compareTo(b.createdAt()));
        }
        return result;
    }

    private void addWaitingEntry(
            Map<Long, List<SeatWaitingStatusResponse.WaitingStudent>> result,
            SeatChangeRequest req, Seat desiredSeat, int priority) {
        if (desiredSeat == null) {
            return;
        }
        result.computeIfAbsent(desiredSeat.getId(), k -> new ArrayList<>())
            .add(new SeatWaitingStatusResponse.WaitingStudent(
                req.getId(),
                req.getStudent().getName(),
                req.getStudent().getStudentNumber(),
                priority,
                req.getCreatedAt()
            ));
    }

    public SeatWaitingStatusResponse findSeatWaitingStatusBySeatId(Long seatId) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!seat.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }

        List<SeatChangeRequest> pendingRequests =
            seatChangeRequestRepository.findAllByStoreIdAndStatusWithStudent(
                seat.getStore().getId(), SeatChangeRequestStatus.PENDING);

        String assignedStudentName = studentRepository
            .findAllWithAssignedSeatByStoreId(seat.getStore().getId()).stream()
            .filter(s -> s.getAssignedSeat().getId().equals(seatId))
            .map(Student::getName)
            .findFirst()
            .orElse(null);

        List<SeatWaitingStatusResponse.WaitingStudent> waitingList = new ArrayList<>();
        for (SeatChangeRequest req : pendingRequests) {
            int priority = getPriorityForSeat(req, seatId);
            if (priority > 0) {
                waitingList.add(new SeatWaitingStatusResponse.WaitingStudent(
                    req.getId(),
                    req.getStudent().getName(),
                    req.getStudent().getStudentNumber(),
                    priority,
                    req.getCreatedAt()
                ));
            }
        }
        waitingList.sort((a, b) -> a.createdAt().compareTo(b.createdAt()));

        return new SeatWaitingStatusResponse(
            seat.getId(),
            seat.getSeatCd(),
            seat.getSeatLabel(),
            seat.getSeatType().name(),
            assignedStudentName,
            waitingList.size(),
            waitingList
        );
    }

    private int getPriorityForSeat(SeatChangeRequest request, Long seatId) {
        if (request.getDesiredSeat1() != null
                && request.getDesiredSeat1().getId().equals(seatId)) {
            return 1;
        }
        if (request.getDesiredSeat2() != null
                && request.getDesiredSeat2().getId().equals(seatId)) {
            return 2;
        }
        if (request.getDesiredSeat3() != null
                && request.getDesiredSeat3().getId().equals(seatId)) {
            return 3;
        }
        return 0;
    }

    private int getPriorityForSeatCd(SeatChangeRequest request, String seatCd) {
        if (request.getDesiredSeat1() != null
                && seatCd.equals(request.getDesiredSeat1().getSeatCd())) {
            return 1;
        }
        if (request.getDesiredSeat2() != null
                && seatCd.equals(request.getDesiredSeat2().getSeatCd())) {
            return 2;
        }
        if (request.getDesiredSeat3() != null
                && seatCd.equals(request.getDesiredSeat3().getSeatCd())) {
            return 3;
        }
        return 0;
    }

    private Seat getSeatByPriority(SeatChangeRequest request, int priority) {
        return switch (priority) {
            case 1 -> request.getDesiredSeat1();
            case 2 -> request.getDesiredSeat2();
            case 3 -> request.getDesiredSeat3();
            default -> throw new SeatChangeRequestException(ErrorCode.SEAT_NOT_IN_REQUEST);
        };
    }

    private void validateStoreAccess(SeatChangeRequest request) {
        if (!SecurityUtil.isAdmin()) {
            Long storeId = SecurityUtil.getCurrentStoreId();
            if (!request.getStore().getId().equals(storeId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    private Seat findSeatInStore(Long seatId, Long storeId) {
        Seat seat = seatRepository.findByIdWithStore(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.getStore().getId().equals(storeId)) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_NOT_IN_THIS_STORE);
        }

        if (!seat.isActive()) {
            throw new SeatException(ErrorCode.SEAT_NOT_FOUND);
        }

        return seat;
    }

    private Seat findSeatByCdInStore(String seatCd, Long storeId) {
        Seat seat = seatRepository.findBySeatCdAndStoreId(seatCd, storeId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.isActive()) {
            throw new SeatException(ErrorCode.SEAT_NOT_FOUND);
        }

        return seat;
    }

    private void validateNotCurrentSeat(Student student, Seat... desiredSeats) {
        if (student.getAssignedSeat() == null) {
            return;
        }
        Long currentSeatId = student.getAssignedSeat().getId();
        for (Seat seat : desiredSeats) {
            if (seat != null && seat.getId().equals(currentSeatId)) {
                throw new SeatChangeRequestException(ErrorCode.DESIRED_SEAT_IS_CURRENT);
            }
        }
    }

    private void handleRedisOnSeatChange(Student student, Seat oldSeat, Seat newSeat) {
        Long storeId = student.getStore().getId();
        Map<Object, Object> redisStatus = seatRedisService.getSeatStatusMap(storeId);

        if (oldSeat != null) {
            String oldStatus = (String) redisStatus.get(oldSeat.getId().toString());
            if (oldStatus != null && oldStatus.contains(":" + student.getId() + ":")) {
                seatRedisService.releaseSeat(storeId, oldSeat.getId());
                seatRedisService.markSeatInUse(storeId, newSeat.getId(),
                    student.getId(), student.getName());
            }
        }
    }
}
