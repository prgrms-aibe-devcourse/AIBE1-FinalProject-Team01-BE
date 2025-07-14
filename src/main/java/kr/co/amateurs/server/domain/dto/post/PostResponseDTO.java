package kr.co.amateurs.server.domain.dto.post;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        Long postId,
        boolean isBlinded,
        BoardType boardType,
        String title,
        String nickname,
        String profileImageUrl,
        DevCourseTrack devCourseTrack,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        String tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public PostResponseDTO applyBlindFilter() {
        if (isBlinded) {
            return new PostResponseDTO(
                this.id,
                this.postId,
                this.isBlinded,
                this.boardType,
                "블라인드 처리된 게시글입니다.",
                this.nickname,
                this.profileImageUrl,
                this.devCourseTrack,
                this.likeCount,
                this.viewCount,
                this.commentCount,
                "",
                this.createdAt,
                this.updatedAt
            );
        }
        return this;
    }
}
