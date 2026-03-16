package com.moduletest.deasungkioskbackend.domain.seatchangerequest.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.AvailableSeatResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatWaitingStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequestStatus;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.exception.SeatChangeRequestException;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository.SeatChangeRequestRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public SeatChangeRequestResponse createRequest(SeatChangeRequestCreateRequest request,
                                                   Long storeId) {
        Student student = studentResolverService.resolveAuto(
            request.identifier(), storeId);

        if (!student.getStore().getId().equals(storeId)) {
            throw new SeatChangeRequestException(ErrorCode.STUDENT_NOT_IN_THIS_STORE);
        }

        if (seatChangeRequestRepository.existsByStudentIdAndStatus(
                student.getId(), SeatChangeRequestStatus.PENDING)) {
            throw new SeatChangeRequestException(ErrorCode.ALREADY_PENDING_SEAT_CHANGE);
        }

        Seat desiredSeat1 = findSeatInStore(request.desiredSeatId1(), storeId);
        Seat desiredSeat2 = request.desiredSeatId2() != null
            ? findSeatInStore(request.desiredSeatId2(), storeId) : null;
        Seat desiredSeat3 = request.desiredSeatId3() != null
            ? findSeatInStore(request.desiredSeatId3(), storeId) : null;

        validateNotCurrentSeat(student, desiredSeat1, desiredSeat2, desiredSeat3);

        SeatChangeRequest seatChangeRequest = SeatChangeRequest.builder()
            .student(student)
            .store(student.getStore())
            .currentSeat(student.getAssignedSeat())
            .desiredSeat1(desiredSeat1)
            .desiredSeat2(desiredSeat2)
            .desiredSeat3(desiredSeat3)
            .build();

        SeatChangeRequest saved = seatChangeRequestRepository.save(seatChangeRequest);
        log.info("좌석 변경 신청: studentId={}, 1순위={}, 2순위={}, 3순위={}",
            student.getId(),
            desiredSeat1.getSeatLabel(),
            desiredSeat2 != null ? desiredSeat2.getSeatLabel() : "없음",
            desiredSeat3 != null ? desiredSeat3.getSeatLabel() : "없음");

        return SeatChangeRequestResponse.fromEntity(saved);
    }

    public List<AvailableSeatResponse> findAvailableSeats(Long storeId) {
        List<Seat> allSeats = seatRepository.findAllByStoreIdWithStore(storeId);
        List<Student> allStudents = studentRepository.findAllByStoreIdWithStore(storeId);

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
        return SeatChangeRequestResponse.fromEntity(request);
    }

    @Transactional
    public SeatChangeRequestResponse approveRequest(Long requestId) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));

        if (request.getStatus() != SeatChangeRequestStatus.PENDING) {
            throw new SeatChangeRequestException(ErrorCode.SEAT_CHANGE_ALREADY_PROCESSED);
        }

        Seat availableSeat = findFirstAvailableSeat(request);

        Student student = studentRepository.findByIdForUpdate(request.getStudent().getId())
            .orElseThrow(() -> new SeatChangeRequestException(ErrorCode.STUDENT_NOT_FOUND));

        Seat oldSeat = student.getAssignedSeat();

        student.assignSeat(availableSeat);
        request.approve(availableSeat);

        handleRedisOnSeatChange(student, oldSeat, availableSeat);

        log.info("좌석 변경 승인: requestId={}, studentId={}, {} -> {}",
            requestId, student.getId(),
            oldSeat != null ? oldSeat.getSeatLabel() : "없음",
            availableSeat.getSeatLabel());

        return SeatChangeRequestResponse.fromEntity(request);
    }

    @Transactional
    public SeatChangeRequestResponse rejectRequest(Long requestId) {
        SeatChangeRequest request = seatChangeRequestRepository.findByIdWithDetails(requestId)
            .orElseThrow(() -> new SeatChangeRequestException(
                ErrorCode.SEAT_CHANGE_REQUEST_NOT_FOUND));

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
        List<Student> allStudents = studentRepository.findAllByStoreIdWithStore(storeId);
        List<SeatChangeRequest> pendingRequests =
            seatChangeRequestRepository.findAllByStoreIdAndStatusWithStudent(
                storeId, SeatChangeRequestStatus.PENDING);

        Map<Long, String> seatIdToStudentName = allStudents.stream()
            .filter(s -> s.getAssignedSeat() != null)
            .collect(Collectors.toMap(
                s -> s.getAssignedSeat().getId(),
                Student::getName,
                (a, b) -> a
            ));

        return allSeats.stream()
            .filter(Seat::isActive)
            .map(seat -> {
                List<SeatWaitingStatusResponse.WaitingStudent> waitingList = new ArrayList<>();
                for (SeatChangeRequest req : pendingRequests) {
                    int priority = getPriorityForSeat(req, seat.getId());
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
                    seat.getSeatLabel(),
                    seat.getSeatType().name(),
                    seatIdToStudentName.get(seat.getId()),
                    waitingList.size(),
                    waitingList
                );
            })
            .toList();
    }

    private int getPriorityForSeat(SeatChangeRequest request, Long seatId) {
        if (request.getDesiredSeat1().getId().equals(seatId)) {
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

    private Seat findFirstAvailableSeat(SeatChangeRequest request) {
        for (Seat seat : request.getDesiredSeatsInOrder()) {
            if (!studentRepository.existsByAssignedSeatId(seat.getId())) {
                return seat;
            }
        }
        throw new SeatChangeRequestException(ErrorCode.DESIRED_SEAT_ALREADY_ASSIGNED);
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
