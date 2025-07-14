package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        Long postId,
        BoardType boardType,
        String title,
        String content,
        String nickname,
        String profileImageUrl,
        DevCourseTrack devCourseTrack,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        String tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}
