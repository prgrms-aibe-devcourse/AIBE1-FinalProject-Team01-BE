package kr.co.amateurs.server.domain.dto.post;

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
) {}