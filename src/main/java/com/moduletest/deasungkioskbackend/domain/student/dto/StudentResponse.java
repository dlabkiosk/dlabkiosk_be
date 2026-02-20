package com.moduletest.deasungkioskbackend.domain.student.dto;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.time.LocalDateTime;

public record StudentResponse(
    Long id,
    Long storeId,
    String storeName,
    String name,
    String phone,
    String qrUuid,
    String grade,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static StudentResponse fromEntity(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getStore().getId(),
            student.getStore().getStoreName(),
            student.getName(),
            student.getPhone(),
            student.getQrUuid(),
            student.getGrade(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
}
