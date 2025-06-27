package kr.co.amateurs.server.domain.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectResponseDTO(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "게시글 ID", example = "1")
        Long postId,
        @Schema(description = "게시글 제목", example = "test 제목")
        String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        String content,
        @Schema(description = "작성자 닉네임", example = "test닉네임")
        String nickname,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
        @Schema(description = "작성자 수강 코스 이름", example = "AIBE")
        String devcourseTrack,
        @Schema(description = "작성자 수강 코스 기수", example = "1")
        String devcourseBatch,
        @Schema(description = "데모 사이트 배포 URL", example = "https://test.com")
        String demoUrl,
        @Schema(description = "깃허브 URL", example = "https://github.com/test/test")
        String githubUrl,
        @Schema(description = "프로젝트 요약", example = "test 프로젝트입니다.")
        String simpleContent,
        @Schema(description = "프로젝트 참여 인원", example = "홍길동")
        String projectMembers,
        @Schema(description = "북마크 수", example = "3")
        Integer bookmarkCount,
        @Schema(description = "썸네일 이미지 존재 여부", example = "false")
        boolean hasImages,
        @Schema(description = "북마크 여부", example = "false")
        boolean hasBookmarked,
        @Schema(description = "프로젝트 시작 일시", example = "2025-06-25T00:08:25")
        LocalDateTime startedAt,
        @Schema(description = "프로젝트 종료 일시", example = "2025-06-25T00:08:25")
        LocalDateTime endedAt,
        @Schema(description = "생성 일시", example = "2025-06-25T00:08:25")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시", example = "2025-06-25T00:08:25")
        LocalDateTime updatedAt
//        String thumbnailImageUrl
) {
    public static ProjectResponseDTO from(Project project) {
        Post post = project.getPost();
        return projectBuilder(project, post).build();
    }

    public static ProjectResponseDTO from(Project project, Integer bookmarkCount) {
        Post post = project.getPost();
        return projectBuilder(project, post, bookmarkCount).build();
    }

    public static ProjectResponseDTO from(Project project, Integer bookmarkCount, boolean hasBookmarked) {
        Post post = project.getPost();
        return projectBuilderWithLoginUser(project, post, bookmarkCount, hasBookmarked).build();
    }

    // TODO: 썸네일 이미지 처리를 어떻게 할 지 확인한 후 코드 수정 예정
//    public static ProjectResponseDTO from(Project project, Post post, String thumbnailImageUrl) {
//        return projectBuilder(project, post)
//                .thumbnailImageUrl(thumbnailImageUrl)
//                .build();
//    }

    private static ProjectResponseDTOBuilder projectBuilder(Project project, Post post) {
        return ProjectResponseDTO.builder()
                .projectId(project.getId())
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(post.getUser().getNickname())
                .tags(post.getTags())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .devcourseTrack(post.getUser().getDevcourseName().toString())
                .devcourseBatch(post.getUser().getDevcourseBatch())
                .startedAt(project.getStartedAt())
                .endedAt(project.getEndedAt())
                .demoUrl(project.getDemoUrl())
                .githubUrl(project.getGithubUrl())
                .simpleContent(project.getSimpleContent())
                .projectMembers(project.getProjectMembers())
                .hasImages(post.getPostImages() != null && !post.getPostImages().isEmpty());
    }

    private static ProjectResponseDTOBuilder projectBuilder(Project project, Post post, Integer bookmarkCount) {
        return projectBuilder(project, post)
                .bookmarkCount(bookmarkCount);
    }

    private static ProjectResponseDTOBuilder projectBuilderWithLoginUser(Project project, Post post, Integer bookmarkCount,
                                                                         boolean hasBookmarked) {
        return projectBuilder(project, post, bookmarkCount)
                .hasBookmarked(hasBookmarked);
    }
}

