package kr.co.amateurs.server.domain.dto.together;

public record UpdatePostRequestDTO(
        String title,
        String content,
        String tags
) {
}
