package kr.co.amateurs.server.domain.dto.together;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record MarketPostResponseDTO(
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
        @Schema(description = "댓글수", example = "3")
        Integer commentCount,
        @Schema(description = "좋아요 수", example = "1")
        Integer likeCount,
        @Schema(description = "북마크 수", example = "1")
        Integer bookmarkCount,
        @Schema(description = "장터 물품 판매 상태", example = "SELLING")
        MarketStatus status,
        @Schema(description = "장터 물품 가격", example = "10000")
        Integer price,
        @Schema(description = "물품 판매 지역", example = "서울")
        String place,
        @Schema(description = "썸네일", example = "http://localimage.com")
        String thumbnail,
        @Schema(description = "생성 일시", example = "2025-06-25T00:08:25")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시", example = "2025-06-25T00:08:25")
        LocalDateTime updatedAt,
        @Schema(description = "좋아요 여부", example = "false")
        boolean hasLiked,
        @Schema(description = "북마크 여부", example = "false")
        boolean hasBookmarked
) {
            public static MarketPostResponseDTO convertToDTO(MarketItem mi, Post post) {
                return new MarketPostResponseDTO(
                        mi.getId(),
                        post.getId(),
                        post.getIsBlinded(),
                        post.getUser().getNickname(),
                        post.getUser().getDevcourseName(),
                        post.getUser().getDevcourseBatch(),
                        post.getUser().getImageUrl(),
                        post.getTitle(),
                        post.getContent(),
                        post.getTags(),
                        0,
                        0,
                        0,
                        0,
                        mi.getStatus(),
                        mi.getPrice(),
                        mi.getPlace(),
                        "",
                        post.getCreatedAt(),
                        post.getUpdatedAt(),
                        false,
                        false
                );
            }

        public MarketPostResponseDTO applyBlindFilter() {
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
                        return new MarketPostResponseDTO(
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
                                this.commentCount,
                                this.likeCount,
                                this.bookmarkCount,
                                this.status,
                                this.price,
                                this.place,
                                this.thumbnail,
                                this.createdAt,
                                this.updatedAt,
                                this.hasLiked,
                                this.hasBookmarked
                        );
                }
                return this;
        }
}
