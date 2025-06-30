package kr.co.amateurs.server.domain.dto.post;

import java.time.LocalDate;

public record PopularPostRequest(
        Long postId,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        Double popularityScore,
        LocalDate calculatedDate
) {
    public static PopularPostRequest of(
            Long postId,
            Integer viewCount,
            Integer likeCount,
            Integer commentCount,
            Double popularityScore,
            LocalDate calculatedDate) {
        return new PopularPostRequest(postId, viewCount, likeCount, commentCount,
                popularityScore, calculatedDate);
    }
}