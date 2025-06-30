package kr.co.amateurs.server.domain.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.amateurs.server.domain.dto.post.PostRequest;

public record CommunityRequestDTO(
        @Schema(
                description = "게시글 제목",
                example = "Spring Boot 관련 질문입니다",
                maxLength = 50
        )
        @NotBlank(message = "제목은 빈 내용일 수 없습니다.")
   String title,
        @Schema(
                description = "게시글 태그 (콤마로 구분)",
                example = "Spring,Java,백엔드",
                maxLength = 50
        )
   String tags,
        @Schema(
                description = "게시글 내용",
                example = "Spring Boot에서 JPA를 사용할 때 N+1 문제를 어떻게 해결해야 할까요?",
                maxLength = 3000
        )
        @NotBlank(message = "내용은 빈 내용일 수 없습니다.")
   String content
) implements PostRequest {
}
