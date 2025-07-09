package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

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
        DevCourseTrack authorDevcourseName,
        LocalDateTime postCreatedAt,
        String title,
        BoardType boardType
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