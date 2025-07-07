package kr.co.amateurs.server.domain.dto.bookmark;


import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.*;

import java.time.LocalDateTime;


public interface BookmarkResponseDTO {
    @Schema(description = "게시글 ID", example = "1")
    Long postId();
    @Schema(description = "게시판 종류", example = "GATHER")
    BoardType boardType();
    @Schema(description = "게시글 제목", example = "test 제목")
    String title();
    @Schema(description = "게시글 내용", example = "test 내용")
    String content();
    @Schema(description = "좋아요 수", example = "1")
    Integer likeCount();
    @Schema(description = "조회수", example = "3")
    Integer viewCount();
    @Schema(description = "게시글 태그", example = "Spring Boot")
    String tag();
    @Schema(description = "생성 일시", example = "2025-06-25T00:08:25")
    LocalDateTime createdAt();
    @Schema(description = "수정 일시", example = "2025-06-25T00:08:25")
    LocalDateTime updatedAt();
}