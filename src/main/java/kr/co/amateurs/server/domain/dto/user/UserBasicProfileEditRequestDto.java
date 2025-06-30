package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserBasicProfileEditRequestDto (
        @Schema(description = "이름", example = "김개발")
        String name,

        @Schema(description = "닉네임", example = "kimcoding")
        String nickname,

        @Schema(description = "프로필 이미지")
        String imageUrl
){
}
