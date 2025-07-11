package kr.co.amateurs.server.domain.dto.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
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
            @Schema(description = "장터 물품 가격", example = "10000")
            int price,
            @Schema(description = "장터 물품 판매 상태", example = "SELLING")
            MarketStatus status,
            @Schema(description = "물품 판매 지역", example = "서울")
            String place
    ) {}
    public static MarketBookmarkDTO convertToDTO(MarketItem mi, PostStatistics postStatistics) {
        Post p = mi.getPost();
        MarketItemInfo mii = new MarketItemInfo(
                mi.getPrice(),
                mi.getStatus(),
                mi.getPlace()
        );
        return new MarketBookmarkDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                postStatistics.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                mii
        );
    }
}
