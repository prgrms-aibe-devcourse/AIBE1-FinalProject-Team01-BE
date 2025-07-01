package kr.co.amateurs.server.domain.dto.post;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PopularPostRequest(
        Long postId,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        Double popularityScore,
        LocalDate calculatedDate,
        String authorNickname,
        String authorDevcourseName,
        LocalDateTime postCreatedAt,
        String title,
        String boardType
) {
    public static PopularPostRequest withScore(
            PopularPostRequest post,
            Double popularityScore,
            LocalDate calculatedDate
    ) {
        return new PopularPostRequest(post.postId(),
                post.viewCount(),
                post.likeCount(),
                post.commentCount(),
                popularityScore,
                calculatedDate,
                post.authorNickname(),
                post.authorDevcourseName(),
                post.postCreatedAt(),
                post.title(),
                post.boardType()
        );
    }
}