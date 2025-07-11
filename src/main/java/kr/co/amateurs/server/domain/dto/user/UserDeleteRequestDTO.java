package kr.co.amateurs.server.domain.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequestDTO(
        String currentPassword
) {

}
