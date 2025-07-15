package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

import java.time.LocalDateTime;

public record PopularPostResponse(
        Long id,
        String title,
        String authorNickname,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        BoardType boardType,
        LocalDateTime createdAt,
        Long boardId
) {
    public static PopularPostResponse from(PopularPostRequest request) {
        return new PopularPostResponse(
                request.postId(),
                request.title(),
                request.authorNickname(),
                request.likeCount() != null ? request.likeCount() : 0,
                request.viewCount() != null ? request.viewCount() : 0,
                request.commentCount() != null ? request.commentCount() : 0,
                request.boardType(),
                request.postCreatedAt(),
                request.boardId()
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
                this.createdAt,
                this.boardId
        );
    }
}