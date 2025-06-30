package kr.co.amateurs.server.domain.dto.ai;

public record PostContentData(
        Long postId,
        String title,
        String content,
        String activityType
) {
}
