package kr.co.amateurs.server.domain.dto.bookmark;


import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.*;

import java.time.LocalDateTime;

//TODO - null 사용을 줄이기 위한 인터페이스화

public interface BookmarkResponseDTO {
    Long postId();
    BoardType boardType();
    String title();
    String content();
    Integer likeCount();
    Integer viewCount();
    String tag();
    LocalDateTime createdAt();
    LocalDateTime updatedAt();
}