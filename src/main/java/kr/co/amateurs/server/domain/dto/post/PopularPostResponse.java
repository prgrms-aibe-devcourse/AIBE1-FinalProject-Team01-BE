package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.entity.post.Post;

import java.time.LocalDateTime;

public record PopularPostResponse(
        Long id,
        String title,
        String authorNickname,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        String boardType,
        LocalDateTime createdAt
) {
    public static PopularPostResponse from(Post post) {
        return new PopularPostResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getNickname(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getComments().size(),
                post.getBoardType().toString(),
                post.getCreatedAt()
        );
    }

    public PostRecommendationResponse toRecommendationResponse() {
        return new PostRecommendationResponse(
                this.id,
                this.title,
                this.authorNickname,
                this.likeCount,
                this.viewCount,
                this.commentCount,
                this.boardType,
                this.createdAt
        );
    }
}