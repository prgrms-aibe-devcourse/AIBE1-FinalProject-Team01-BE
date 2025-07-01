package kr.co.amateurs.server.domain.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProjectRequestDTO(
        @Schema(description = "게시글 제목", example = "test 제목")
        @NotBlank String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        @NotBlank String content,
        @Schema(description = "게시글 태그", example = "Spring boot")
        String tags,
        @Schema(description = "프로젝트 시작 일시", example = "2025-06-25T00:08:25")
        @NotNull LocalDateTime startedAt,
        @Schema(description = "프로젝트 종료 일시", example = "2025-06-25T00:08:25")
        @NotNull LocalDateTime endedAt,
        @Schema(description = "깃허브 URL", example = "https://github.com/test/test")
        String githubUrl,
        @Schema(description = "프로젝트 요약", example = "test 프로젝트입니다.")
        String simpleContent,
        @Schema(description = "데모 사이트 배포 URL", example = "https://test.com")
        String demoUrl,
        @Schema(description = "프로젝트 참여 인원", example = "홍길동")
        @NotNull String projectMembers
) {
}
