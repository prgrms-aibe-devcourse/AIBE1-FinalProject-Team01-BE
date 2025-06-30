package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;

import java.time.LocalDateTime;

public record MatchingBookmarkDTO(
        Long postId,
        BoardType boardType,
        String title,
        String content,
        Integer likeCount,
        Integer viewCount,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        MatchingPostInfo matchingPostInfo
) implements BookmarkResponseDTO {
    public record MatchingPostInfo(
            MatchingType matchingType,
            MatchingStatus status,
            String expertiseAreas
    ) {}
    public static MatchingBookmarkDTO convertToDTO(MatchingPost mp) {
        Post p = mp.getPost();
        MatchingPostInfo mpi = new MatchingPostInfo(
                mp.getMatchingType(),
                mp.getStatus(),
                mp.getExpertiseAreas()
        );
        return new MatchingBookmarkDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                mpi
        );
    }
}
