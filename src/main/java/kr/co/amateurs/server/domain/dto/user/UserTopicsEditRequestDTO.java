package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserTopicsEditRequestDTO(

        @Schema(description = "관심 주제 목록", example = "[\"FRONTEND\", \"BACKEND\"]")
        Set<Topic> topics
) {
}
