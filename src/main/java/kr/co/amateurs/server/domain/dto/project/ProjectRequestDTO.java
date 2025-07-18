package kr.co.amateurs.server.domain.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProjectRequestDTO(
        @NotBlank(message = "제목은 빈 내용일 수 없습니다.")
        @Size(max = 100, message = "최대 100자까지 입력할 수 있습니다.")
        @Schema(description = "게시글 제목", example = "test 제목")
        String title,
        @NotBlank(message = "내용은 빈 내용일 수 없습니다.")
        @Schema(description = "게시글 내용", example = "test 내용")
        String content,
        @Schema(description = "게시글 태그", example = "[\"Java\", \"SpringBoot\", \"React\", \"WebSocket\"]")
        List<String> tags,
        @Schema(description = "프로젝트 시작 일시", example = "2025-06-25T00:08:25")
        LocalDateTime startedAt,
        @Schema(description = "프로젝트 종료 일시", example = "2025-06-25T00:08:25")
        LocalDateTime endedAt,
        @Schema(description = "깃허브 URL", example = "https://github.com/test")
        @Pattern(
                regexp = "^https://github\\.com/[a-zA-Z0-9-]+/[a-zA-Z0-9._-]+/?$",
                message = "올바른 깃허브 URL 형식이 아닙니다"
        )
        String githubUrl,
        @Schema(description = "프로젝트 요약", example = "test 프로젝트입니다.")
        String simpleContent,
        @Pattern(
                regexp = "^https://(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}(/.*)?$",
                message = "올바른 URL 형식이 아닙니다 (https://example.com 또는 https://www.example.com)"
        )
        @Schema(description = "데모 사이트 배포 URL", example = "https://test.com")
        String demoUrl,
        @Schema(
                description = "프로젝트 참여 인원 (이름 및 역할)",
                example = "[{\"name\": \"김길동\", \"role\": \"백엔드\"}, {\"name\": \"이영희\", \"role\": \"프론트엔드\"}]"
        )
        List<ProjectMember> projectMembers
) {
}