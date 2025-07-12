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
        @Schema(description = "불러안두 여부", example = "false")
        boolean isBlinded,
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
        @Schema(description = "북마크 수", example = "1")
        Integer bookmarkCount,
        @Schema(description = "팀원 모집 종류", example = "STUDY")
        GatheringType gatheringType,
        @Schema(description = "팀원 모집 상태", example = "RECRUITING")
        GatheringStatus status,
        @Schema(description = "모집 인원", example = "4")
        Integer headCount,
        @Schema(description = "모임 장소", example = "서울")
        String place,
        @Schema(description = "모임 기간", example = "250625 ~ 250627")
        String period,
        @Schema(description = "모임 일정", example = "매주 화, 목 저녁 7시")
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
    public static GatheringPostResponseDTO convertToDTO(GatheringPost gp, Post post, PostStatistics postStatistics, boolean hasLiked, boolean hasBookmarked, Integer bookmarkCount) {
        return new GatheringPostResponseDTO(
                gp.getId(),
                post.getId(),
                post.getIsBlinded(),
                post.getUser().getNickname(),
                post.getUser().getDevcourseName(),
                post.getUser().getDevcourseBatch(),
                post.getUser().getImageUrl(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                postStatistics.getViewCount(),
                post.getLikeCount(),
                bookmarkCount,
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

        public GatheringPostResponseDTO applyBlindFilter() {
                if (this.isBlinded) {
                        String blindedContent = """
                            <div style="text-align: center; padding: 60px 20px; background-color: #f8f9fa; border-radius: 8px; margin: 20px 0;">
                                <div style="font-size: 24px; font-weight: bold; color: #6c757d; margin-bottom: 10px;">
                                    ⚠️ 블라인드 처리된 게시글입니다
                                </div>
                                <div style="font-size: 16px; color: #868e96;">
                                    관리자가 처리 중입니다.
                                </div>
                            </div>
                            """;
                        return new GatheringPostResponseDTO(
                                this.id,
                                this.postId,
                                this.isBlinded,
                                this.nickname,
                                this.devcourseName,
                                this.devcourseBatch,
                                this.userProfileImg,
                                "블라인드 처리된 게시글입니다.",
                                blindedContent,
                                "",
                                this.viewCount,
                                this.likeCount,
                                this.bookmarkCount,
                                this.gatheringType,
                                this.status,
                                this.headCount,
                                this.place,
                                this.period,
                                this.schedule,
                                this.createdAt,
                                this.updatedAt,
                                this.hasImages,
                                this.hasLiked,
                                this.hasBookmarked
                        );
                }
                return this;
        }
}