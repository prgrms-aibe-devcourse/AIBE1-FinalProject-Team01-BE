package kr.co.amateurs.server.domain.dto.community;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityResponseDTO {
    private Long postId;
    private String title;
    private String content;
    private String authorName;
    private BoardType boardType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private String thumbnailImage;
    private boolean hasImages;

    public static CommunityResponseDTO from(Post post, int commentCount, List<String> tagList, String thumbnailImage, boolean hasImages) {
        return CommunityResponseDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent().length() > 100 ?
                        post.getContent().substring(0, 100) + "..." :
                        post.getContent())
                .authorName(post.getUser().getNickname())
                .boardType(post.getBoardType())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(tagList)
                .thumbnailImage(thumbnailImage)
                .hasImages(hasImages)
                .build();
    }
}
