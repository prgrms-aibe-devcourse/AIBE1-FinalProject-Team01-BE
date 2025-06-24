package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.*;

import java.time.LocalDateTime;

public record BookmarkResponseDTO(
        Long postId,
        BoardType boardType,
        String title,
        String content,
        Integer likeCount,
        Integer viewCount,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        GatheringInfo gatheringInfo,
        MarketItemInfo marketItemInfo,
        MatchingPostInfo matchingPostInfo
) {
    public record GatheringInfo(
            GatheringType gatheringType,
            GatheringStatus status,
            int headCount,
            String place,
            String period,
            String schedule
    ) {}

    public record MarketItemInfo(
            int price,
            MarketStatus status
    ) {}

    public record MatchingPostInfo(
            MatchingType matchingType,
            MatchingStatus status,
            String expertiseAreas
    ) {}


    public static BookmarkResponseDTO convertToPostDTO(Bookmark b) {
        Post p = b.getPost();
        return new BookmarkResponseDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                null,
                null,
                null
        );
    }

    public static BookmarkResponseDTO convertToGatheringDTO(Bookmark b, GatheringPost gp) {
        Post p = b.getPost();
        GatheringInfo gi = new GatheringInfo(
                gp.getGatheringType(),
                gp.getStatus(),
                gp.getHeadCount(),
                gp.getPlace(),
                gp.getPeriod(),
                gp.getSchedule()
        );
        return new BookmarkResponseDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                gi,
                null,
                null
        );
    }

    public static BookmarkResponseDTO convertToMatchingDTO(Bookmark b, MatchingPost mp) {
        Post p = b.getPost();
        MatchingPostInfo mpi = new MatchingPostInfo(
                mp.getMatchingType(),
                mp.getStatus(),
                mp.getExpertiseAreas()
        );
        return new BookmarkResponseDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                null,
                null,
                mpi
        );
    }

    public static BookmarkResponseDTO convertToMarketDTO(Bookmark b, MarketItem mi){
        Post p = b.getPost();
        MarketItemInfo mii = new MarketItemInfo(
                mi.getPrice(),
                mi.getStatus()
        );
        return new BookmarkResponseDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                null,
                mii,
                null
        );
    }
}
