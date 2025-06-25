package kr.co.amateurs.server.domain.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityRequestDTO(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
   String title,
   String tags,
        @NotBlank(message = "내용은 필수입니다.")
   String content
) {
}
