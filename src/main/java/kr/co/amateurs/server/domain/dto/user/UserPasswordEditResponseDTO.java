package kr.co.amateurs.server.domain.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserPasswordEditResponseDTO(

        @Schema(description = "성공 메시지", example = "비밀번호가 성공적으로 변경되었습니다")
        String message
) {
}
