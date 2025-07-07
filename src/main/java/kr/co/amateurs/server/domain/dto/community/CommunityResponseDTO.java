package kr.co.amateurs.server.domain.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDateTime;

public record CommunityResponseDTO(
        @Schema(description = "커뮤니티 ID", example = "1")
        Long communityId,

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "게시글 제목", example = "Spring Boot 관련 질문입니다")
        String title,

        @Schema(description = "게시글 내용", example = "Spring Boot에서 JPA를 사용할 때...")
        String content,

        @Schema(description = "작성자 닉네임", example = "testUser")
        String nickname,

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg", nullable = true)
        String profileImageUrl,

        @Schema(description = "작성자 데브코스 명", example = "AIBE")
        DevCourseTrack devCourseTrack,

        @Schema(description = "작성자 데브코스 기수", example = "1기")
        String devCourseBatch,

        @Schema(description = "게시판 타입", example = "FREE")
        BoardType boardType,

        @Schema(description = "조회수", example = "42")
        Integer viewCount,

        @Schema(description = "좋아요 수", example = "15")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "8")
        Integer commentCount,

        @Schema(description = "게시글 작성 시간", example = "2025-06-27T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "게시글 수정 시간", example = "2025-06-27T14:35:00")
        LocalDateTime updatedAt,

        @Schema(description = "게시글 태그", example = "Spring,Java,백엔드", nullable = true)
        String tags,
        @Schema(description = "현재 사용자가 좋아요를 눌렀는지 여부", example = "false")
        boolean hasLiked,

        @Schema(description = "현재 사용자가 북마크했는지 여부", example = "false")
        boolean hasBookmarked
) {
        public static CommunityResponseDTO from(CommunityPost communityPost, boolean hasLiked, boolean hasBookmarked) {
                Post post = communityPost.getPost();
                return new CommunityResponseDTO(
                        communityPost.getId(),
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getUser().getNickname(),
                        post.getUser().getImageUrl(),
                        post.getUser().getDevcourseName(),
                        post.getUser().getDevcourseBatch(),
                        post.getBoardType(),
                        post.getViewCount(),
                        post.getLikeCount(),
                        0,
                        post.getCreatedAt(),
                        post.getUpdatedAt(),
                        post.getTags(),
                        hasLiked,
                        hasBookmarked
                );
        }
}
