package com.moduletest.deasungkioskbackend.domain.notice.dto;

import com.moduletest.deasungkioskbackend.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공지사항 응답")
public record NoticeResponse(
    @Schema(description = "공지사항 ID", example = "1")
    Long id,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "제목", example = "2월 운영시간 변경 안내")
    String title,
    @Schema(description = "내용", example = "2월부터 운영시간이 변경됩니다.")
    String content,
    @Schema(description = "상단 고정 여부", example = "false")
    boolean pinned,
    @Schema(description = "활성화 여부", example = "true")
    boolean active,
    @Schema(description = "작성자 (전체관리자 또는 지점명)", example = "대성학원 강남점")
    String createdBy,
    @Schema(description = "생성일시")
    LocalDateTime createdAt,
    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

    public static NoticeResponse fromEntity(Notice notice) {
        return new NoticeResponse(
            notice.getId(),
            notice.getStore().getId(),
            notice.getStore().getStoreName(),
            notice.getTitle(),
            notice.getContent(),
            notice.isPinned(),
            notice.isActive(),
            notice.getCreatedBy(),
            notice.getCreatedAt(),
            notice.getUpdatedAt()
        );
    }
}
