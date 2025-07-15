package kr.co.amateurs.server.domain.event;

public record AiProfileEvent(
        Long userId,
        String context
) {
}