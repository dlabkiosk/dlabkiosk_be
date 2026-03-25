package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.repository.PhoneSubmissionRepository;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository.SeatChangeRequestRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentCreateRequest;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentKioskResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;
    private final StudentResolverService studentResolverService;
    private final SeatChangeRequestRepository seatChangeRequestRepository;
    private final PhoneSubmissionRepository phoneSubmissionRepository;
    private final SeatLeaveRepository seatLeaveRepository;
    private final StudentDetailDsaService studentDetailDsaService;

    public List<StudentResponse> findAllStudents(Long storeId) {
        if (storeId != null) {
            return studentRepository.findAllByStoreIdWithStore(storeId)
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
        }
        return studentRepository.findAllWithStore()
            .stream()
            .map(StudentResponse::fromEntity)
            .toList();
    }

    public StudentResponse findStudentById(Long studentId) {
        Student student = studentRepository.findByIdWithStore(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        if (request.studentNumber() != null && !request.studentNumber().isBlank()
            && studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw new StudentException(ErrorCode.DUPLICATE_STUDENT_NUMBER);
        }

        Seat assignedSeat = resolveSeat(request.seatId());

        Student student = Student.builder()
            .store(store)
            .name(request.name())
            .rfidUid(request.rfidUid())
            .studentNumber(request.studentNumber())
            .phoneLast4(request.phoneLast4())
            .assignedSeat(assignedSeat)
            .build();

        Student savedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(savedStudent);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        studentRepository.delete(student);
    }

    public StudentKioskResponse searchStudentForKiosk(String identifier, Long storeId,
                                                      InputMethod inputMethod) {
        Student student = studentResolverService.resolveAuto(
            identifier, storeId, inputMethod);
        Long studentId = student.getId();

        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        List<PhoneSubmissionResponse> phoneSubmissions = phoneSubmissionRepository
            .findAllByStudentIdAndPeriod(studentId, monthStart, monthEnd)
            .stream()
            .map(PhoneSubmissionResponse::fromEntity)
            .toList();

        List<SeatChangeRequestResponse> seatChangeRequests = seatChangeRequestRepository
            .findAllByStudentIdWithDetails(studentId)
            .stream()
            .filter(r -> r.getCreatedAt() != null
                && !r.getCreatedAt().isBefore(monthStart)
                && r.getCreatedAt().isBefore(monthEnd))
            .map(SeatChangeRequestResponse::fromEntity)
            .toList();

        List<SeatLeaveResponse> seatLeaves = seatLeaveRepository
            .findAllByStudentIdAndPeriod(studentId, monthStart, monthEnd)
            .stream()
            .map(SeatLeaveResponse::fromEntity)
            .toList();

        Store store = student.getStore();

        return StudentKioskResponse.of(student,
            phoneSubmissions, seatChangeRequests, seatLeaves,
            studentDetailDsaService.findMealApplications(student.getStudentNumber(), store),
            studentDetailDsaService.findReceipts(student.getRfidUid(), store),
            studentDetailDsaService.findAttendanceSummary(student.getRfidUid(), store),
            studentDetailDsaService.findPoints(student.getStudentNumber(), store));
    }

    private Seat resolveSeat(Long seatId) {
        if (seatId == null) {
            return null;
        }
        return seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatException(ErrorCode.SEAT_NOT_FOUND));
    }

    public byte[] generateStudentQrCode(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        return QrCodeService.generateQrCodePng(student.getRfidUid());
    }
}
