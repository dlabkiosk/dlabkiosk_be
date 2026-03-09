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
    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,
    @Schema(description = "QR 코드용 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,
    @Schema(description = "RFID 카드 UID", example = "A1B2C3D4")
    String rfidUid,
    @Schema(description = "학번", example = "2024-001")
    String studentNumber,
    @Schema(description = "학년", example = "고3")
    String grade,
    @Schema(description = "반", example = "A반")
    String className,
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
            student.getPhone(),
            student.getQrUuid(),
            student.getRfidUid(),
            student.getStudentNumber(),
            student.getGrade(),
            student.getClassName(),
            seatId,
            seatLabel,
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
}
