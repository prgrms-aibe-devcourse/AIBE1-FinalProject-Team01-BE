package kr.co.amateurs.server.domain.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectResponseDTO(
        Long projectId,
        Long postId,
        String title,
        String content,
        String nickname,
        String tags,
        String devcourseTrack,
        String devcourseBatch,
        String demoUrl,
        String githubUrl,
        String simpleContent,
        String projectMembers,
        Integer bookmarkCount,
        boolean hasImages,
        boolean hasBookmarked,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt,
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

