package kr.co.amateurs.server.domain.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectMember(
        @Schema(description = "프로젝트 멤버 이름", example = "김길동")
        String name,
        @Schema(description = "프로젝트에서 맡은 역할", example = "백엔드")
        String role
) {
}
