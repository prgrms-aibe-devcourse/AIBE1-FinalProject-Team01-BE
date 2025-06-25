package kr.co.amateurs.server.domain.dto.file;

public record FileResponseDTO(
        Boolean success,
        String message,
        String url
) {
}
