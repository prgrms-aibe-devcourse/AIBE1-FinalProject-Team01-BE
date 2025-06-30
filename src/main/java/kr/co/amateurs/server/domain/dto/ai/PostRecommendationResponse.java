package kr.co.amateurs.server.domain.dto.ai;

import kr.co.amateurs.server.domain.entity.post.Post;

import java.time.LocalDateTime;

public record PostRecommendationResponse(
        Long id,
        String title,
        String authorNickname,
        Integer likeCount,
        Integer commentCount,
        String boardType,
        LocalDateTime createdAt
) {
    public static PostRecommendationResponse from(Post post) {
        return new PostRecommendationResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getNickname(),
                post.getLikeCount(),
                post.getComments().size(),
                post.getBoardType().toString(),
                post.getCreatedAt()
        );
    }
}
