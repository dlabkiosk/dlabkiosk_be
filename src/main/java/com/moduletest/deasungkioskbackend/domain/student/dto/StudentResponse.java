package com.moduletest.deasungkioskbackend.domain.student.dto;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "학생 응답")
public record StudentResponse(
    @Schema(description = "학생 ID", example = "1")
    Long id,
    @Schema(description = "소속 지점 ID", example = "1")
    Long storeId,
    @Schema(description = "소속 지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "학생 이름", example = "김대성")
    String name,
    @Schema(description = "RFID UID", example = "A1B2C3D4")
    String rfidUid,
    @Schema(description = "학번", example = "2024-001")
    String studentNumber,
    @Schema(description = "배정 좌석 ID")
    Long assignedSeatId,
    @Schema(description = "배정 좌석 라벨", example = "A-1")
    String assignedSeatLabel,
    @Schema(description = "생성일시")
    LocalDateTime createdAt,
    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

    public static StudentResponse fromEntity(Student student) {
        Long seatId = null;
        String seatLabel = null;
        if (student.getAssignedSeat() != null) {
            seatId = student.getAssignedSeat().getId();
            seatLabel = student.getAssignedSeat().getSeatLabel();
        }
        return new StudentResponse(
            student.getId(),
            student.getStore().getId(),
            student.getStore().getStoreName(),
            student.getName(),
            student.getRfidUid(),
            student.getStudentNumber(),
            seatId,
            seatLabel,
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
}
