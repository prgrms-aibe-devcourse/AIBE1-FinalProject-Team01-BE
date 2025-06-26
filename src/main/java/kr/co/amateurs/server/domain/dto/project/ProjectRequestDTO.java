package kr.co.amateurs.server.domain.dto.project;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProjectRequestDTO(
        String title,
        String content,
        String tags,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String githubUrl,
        String simpleContent,
        String demoUrl,
        String projectMembers
) {
}
