package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.exception.SeatException;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository.SeatChangeRequestRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentCreateRequest;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentKioskResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
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
    public StudentResponse updateStudent(Long studentId, StudentUpdateRequest request) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));

        student.updateInfo(request.name(), request.studentNumber(), request.phoneLast4());

        if (request.rfidUid() != null) {
            student.updateRfidUid(request.rfidUid());
        }

        Seat assignedSeat = resolveSeat(request.seatId());
        student.assignSeat(assignedSeat);

        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentException(ErrorCode.STUDENT_NOT_FOUND));
        studentRepository.delete(student);
    }

    public StudentKioskResponse searchStudentForKiosk(String identifier, Long storeId) {
        Student student = studentResolverService.resolveAuto(identifier, storeId);

        SeatChangeRequest latestRequest = seatChangeRequestRepository
            .findAllByStudentIdWithDetails(student.getId())
            .stream()
            .findFirst()
            .orElse(null);

        return StudentKioskResponse.of(student, latestRequest);
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
