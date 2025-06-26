package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

import java.time.LocalDateTime;

public record MarketBookmarkDTO(
        Long postId,
        BoardType boardType,
        String title,
        String content,
        Integer likeCount,
        Integer viewCount,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        MarketItemInfo marketItemInfo
) implements BookmarkResponseDTO {
    public record MarketItemInfo(
            int price,
            MarketStatus status
    ) {}
    public static MarketBookmarkDTO convertToDTO(MarketItem mi) {
        Post p = mi.getPost();
        MarketItemInfo mii = new MarketItemInfo(
                mi.getPrice(),
                mi.getStatus()
        );
        return new MarketBookmarkDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                mii
        );
    }
}
