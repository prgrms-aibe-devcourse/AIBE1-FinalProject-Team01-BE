package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.entity.post.Post;

import java.time.LocalDateTime;

public record PopularPostResponse(
        Long id,
        String title,
        String content,
        Integer viewCount,
        Integer likeCount,
        String authorNickname,
        LocalDateTime createdAt,
        String boardType
) {
    public static PopularPostResponse from(Post post) {
        return new PopularPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getUser().getNickname(),
                post.getCreatedAt(),
                post.getBoardType().toString()
        );
    }
}