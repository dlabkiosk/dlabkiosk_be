package com.moduletest.deasungkioskbackend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 페이징 응답 공통 DTO.
 * Spring Data PageImpl의 주요 필드를 그대로 노출하여 프론트 호환성을 유지한다.
 * (Spring Data 3.3+ PageImpl 직접 직렬화 경고 회피용)
 */
@Schema(description = "페이징 응답")
public record PageResponse<T>(
    @Schema(description = "현재 페이지 데이터")
    List<T> content,
    @Schema(description = "현재 페이지 번호 (0부터 시작)")
    int number,
    @Schema(description = "페이지 크기")
    int size,
    @Schema(description = "전체 페이지 수")
    int totalPages,
    @Schema(description = "전체 데이터 개수")
    long totalElements,
    @Schema(description = "현재 페이지 데이터 개수")
    int numberOfElements,
    @Schema(description = "첫 페이지 여부")
    boolean first,
    @Schema(description = "마지막 페이지 여부")
    boolean last,
    @Schema(description = "비어 있는 페이지 여부")
    boolean empty
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumberOfElements(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }
}
