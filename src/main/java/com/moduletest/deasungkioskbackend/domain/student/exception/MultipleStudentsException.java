package com.moduletest.deasungkioskbackend.domain.student.exception;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.util.List;
import lombok.Getter;

@Getter
public final class MultipleStudentsException extends StudentException {

    private final List<Candidate> candidates;

    public MultipleStudentsException(List<Student> students) {
        super(ErrorCode.MULTIPLE_STUDENTS_FOUND_BY_PHONE_LAST4);
        this.candidates = students.stream()
            .map(s -> new Candidate(
                s.getRfidUid(),
                s.getName(),
                s.getStudentNumber(),
                s.getAssignedSeat() != null ? s.getAssignedSeat().getSeatLabel() : null))
            .toList();
    }

    public record Candidate(
        String identifier,
        String name,
        String studentNumber,
        String seatLabel
    ) {

    }
}
