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
        Long userId,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MatchingType matchingType,
        MatchingStatus status,
        String expertiseArea,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MatchPostResponseDTO convertToDTO(MatchingPost mp, Post post) {
        return new MatchPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mp.getMatchingType(),
                mp.getStatus(),
                mp.getExpertiseAreas(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static Page<MatchPostResponseDTO> convertToDTO(Page<MatchingPost> mpPage) {
        return mpPage.map(mp -> {
            Post post = mp.getPost();
            return convertToDTO(mp, post);
        });
    }
}
