package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record MatchPostResponseDTO(
        Long postId,
        String nickname,
        String devcourseName,
        String devcourseBatch,
        String userProfileImg,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MatchingType matchingType,
        MatchingStatus status,
        String expertiseArea,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean hasImages,
        boolean hasLiked,
        boolean hasBookmarked
) {
    public static MatchPostResponseDTO convertToDTO(MatchingPost mp, Post post, boolean hasLiked, boolean hasBookmarked) {
        return new MatchPostResponseDTO(
                post.getId(),
                post.getUser().getNickname(),
                post.getUser().getDevcourseName(),
                post.getUser().getDevcourseBatch(),
                post.getUser().getImageUrl(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mp.getMatchingType(),
                mp.getStatus(),
                mp.getExpertiseAreas(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPostImages() != null && !post.getPostImages().isEmpty(),
                hasLiked,
                hasBookmarked
        );
    }
}
