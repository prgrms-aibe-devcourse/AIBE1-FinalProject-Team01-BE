package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record MarketPostResponseDTO(
        Long postId,
        Long userId,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MarketStatus status,
        Integer price,
        String place,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MarketPostResponseDTO convertToDTO(MarketItem mi, Post post){
        return new MarketPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mi.getStatus(),
                mi.getPrice(),
                mi.getPlace(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static Page<MarketPostResponseDTO> convertToDTO(Page<MarketItem> miPage) {
        return miPage.map(mi -> {
            Post post = mi.getPost();
            return convertToDTO(mi, post);
        });
    }
}
