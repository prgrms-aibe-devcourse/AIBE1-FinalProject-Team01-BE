package kr.co.amateurs.server.domain.dto.together;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public record GatheringPostRequestDTO(
        @Schema(description = "게시글 제목", example = "test 제목")
        @NotBlank String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        @NotBlank String content,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
        @Schema(description = "팀원 모집 종류", example = "STUDY")
        @NotNull GatheringType gatheringType,
        @Schema(description = "팀원 모집 상태", example = "RECRUITING")
        @NotNull GatheringStatus status,
        @Schema(description = "모집 인원", example = "4")
        @PositiveOrZero Integer headCount,
        @Schema(description = "모임 장소", example = "서울")
        String place,
        @Schema(description = "모임 기간", example = "250625 ~ 250627")
        String period,
        @Schema(description = "모임 일정", example = "매주 화, 목 저녁 7시")
        String schedule
) {}