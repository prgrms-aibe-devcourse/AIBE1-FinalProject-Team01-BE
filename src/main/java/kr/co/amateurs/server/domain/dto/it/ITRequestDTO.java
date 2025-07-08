package kr.co.amateurs.server.domain.dto.it;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.amateurs.server.domain.dto.post.PostRequest;

import java.util.List;

public record ITRequestDTO (
        @Schema(
                description = "게시글 제목",
                example = "프로그래머스 데브코스 백엔드 6기 모집",
                maxLength = 50
        )
        @NotBlank(message = "제목은 빈 내용일 수 없습니다.")
   String title,
        @Schema(
                description = "게시글 태그 (,로 구분)",
                example = "데브코스,백엔드,프로그래머스,모집",
                nullable = true,
                maxLength = 100
        )
        List<String> tags,
        @Schema(
                description = "게시글 내용",
                example = "프로그래머스 데브코스 백엔드 6기에서 함께 공부할 동료를 모집합니다...",
                maxLength = 3000
        )
        @NotBlank(message = "내용은 빈 내용일 수 없습니다.")
   String content
)implements PostRequest {
}
