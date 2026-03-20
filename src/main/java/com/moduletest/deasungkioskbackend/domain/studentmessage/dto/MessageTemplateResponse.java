package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.MessageTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "메시지 템플릿 응답")
public record MessageTemplateResponse(
    @Schema(description = "템플릿 ID", example = "1")
    Long id,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "템플릿 문구", example = "식비가 미납입니다.")
    String content,
    @Schema(description = "생성일시")
    LocalDateTime createdAt
) {

    public static MessageTemplateResponse fromEntity(MessageTemplate template) {
        return new MessageTemplateResponse(
            template.getId(),
            template.getStore().getId(),
            template.getStore().getStoreName(),
            template.getContent(),
            template.getCreatedAt()
        );
    }
}
