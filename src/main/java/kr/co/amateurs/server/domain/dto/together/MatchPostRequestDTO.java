package kr.co.amateurs.server.domain.dto.together;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;

public record MatchPostRequestDTO(
        @Schema(description = "게시글 제목", example = "test 제목")
        @NotNull String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        @NotNull String content,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
        @Schema(description = "커피챗/멘토링 종류", example = "COFFEE CHAT")
        @NotNull MatchingType matchingType,
        @Schema(description = "모집 상태", example = "OPEN")
        @NotNull MatchingStatus status,
        @Schema(description = "전문 분야", example = "백엔드")
        String expertiseArea
) {}