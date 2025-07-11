package kr.co.amateurs.server.domain.dto.together;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record GatheringPostResponseDTO(
        @Schema(description = "팀원구하기 글 ID", example = "1")
        Long id,
        @Schema(description = "게시글 ID", example = "1")
        Long postId,
        @Schema(description = "작성자 닉네임", example = "test닉네임")
        String nickname,
        @Schema(description = "작성자 수강 코스 이름", example = "AIBE")
        DevCourseTrack devcourseName,
        @Schema(description = "작성자 수강 코스 기수", example = "1")
        String devcourseBatch,
        @Schema(description = "작성자 프로필 이미지", example = "test.net/profile/test-img")
        String userProfileImg,
        @Schema(description = "게시글 제목", example = "test 제목")
        String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        String content,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
        @Schema(description = "조회수", example = "3")
        Integer viewCount,
        @Schema(description = "좋아요 수", example = "1")
        Integer likeCount,
        @Schema(description = "팀원 모집 종류", example = "STUDY")
        GatheringType gatheringType,
        GatheringStatus status,
        Integer headCount,
        String place,
        String period,
        String schedule,
        @Schema(description = "생성 일시", example = "2025-06-25T00:08:25")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시", example = "2025-06-25T00:08:25")
        LocalDateTime updatedAt,
        @Schema(description = "썸네일 이미지 존재 여부", example = "false")
        boolean hasImages,
        @Schema(description = "좋아요 여부", example = "false")
        boolean hasLiked,
        @Schema(description = "북마크 여부", example = "false")
        boolean hasBookmarked
) {
    public static GatheringPostResponseDTO convertToDTO(GatheringPost gp, Post post, PostStatistics postStatistics, boolean hasLiked, boolean hasBookmarked) {
        return new GatheringPostResponseDTO(
                gp.getId(),
                post.getId(),
                post.getUser().getNickname(),
                post.getUser().getDevcourseName(),
                post.getUser().getDevcourseBatch(),
                post.getUser().getImageUrl(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                postStatistics.getViewCount(),
                post.getLikeCount(),
                gp.getGatheringType(),
                gp.getStatus(),
                gp.getHeadCount(),
                gp.getPlace(),
                gp.getPeriod(),
                gp.getSchedule(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPostImages() != null && !post.getPostImages().isEmpty(),
                hasLiked,
                hasBookmarked
        );
    }
}