package kr.co.amateurs.server.domain.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDateTime;

import static org.jooq.generated.tables.Posts.POSTS;
import static org.jooq.generated.tables.Projects.PROJECTS;
import static org.jooq.generated.tables.Users.USERS;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectResponseDTO(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "게시글 ID", example = "1")
        Long postId,
        @Schema(description = "프로젝트 시작 일시", example = "2025-06-25T00:08:25")
        LocalDateTime startedAt,
        @Schema(description = "프로젝트 종료 일시", example = "2025-06-25T00:08:25")
        LocalDateTime endedAt,
        @Schema(description = "깃허브 URL", example = "https://github.com/test/test")
        String githubUrl,
        @Schema(description = "프로젝트 요약", example = "test 프로젝트입니다.")
        String simpleContent,
        @Schema(description = "데모 사이트 배포 URL", example = "https://test.com")
        String demoUrl,
        @Schema(description = "프로젝트 참여 인원", example = "홍길동")
        String projectMembers,
        @Schema(description = "게시글 제목", example = "test 제목")
        String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        String content,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
// TODO: 조회수 로직 구현되면 주석 해제
//        @Schema(description = "조회수", example = "15")
//        Integer viewCount,
        @Schema(description = "좋아요 수", example = "10")
        Integer likeCount,
        @Schema(description = "북마크 수", example = "3")
        Integer bookmarkCount,
        @Schema(description = "생성 일시", example = "2025-06-25T00:08:25")
        LocalDateTime createdAt,
        @Schema(description = "수정 일시", example = "2025-06-25T00:08:25")
        LocalDateTime updatedAt,
        @Schema(description = "작성자 닉네임", example = "test닉네임")
        String nickname,
        @Schema(description = "작성자 수강 코스 이름", example = "AIBE")
        String devcourseTrack,
        @Schema(description = "작성자 수강 코스 기수", example = "1")
        String devcourseBatch,
// TODO: 이미지 로직 구현되면 주석 해제
//        @Schema(description = "썸네일 이미지 URL", example = "https://placehold.co/400x400")
//        String thumbnailImageUrl,
//        @Schema(description = "썸네일 이미지 존재 여부", example = "false")
//        boolean hasImages,
        @Schema(description = "북마크 여부", example = "false")
        boolean hasBookmarked,
        @Schema(description = "좋아요 여부", example = "false")
        boolean hasLiked
) {
    public static ProjectResponseDTO from(Project project) {
        return jpaBuilder(project).build();
    }

    public static ProjectResponseDTO from(Record record) {
        return jooqBuilder(record).build();
    }

    public static ProjectResponseDTOBuilder jpaBuilder(Project project) {
        Post post = project.getPost();
        return ProjectResponseDTO.builder()
                .projectId(project.getId())
                .postId(post.getId())
                .startedAt(project.getStartedAt())
                .endedAt(project.getEndedAt())
                .githubUrl(project.getGithubUrl())
                .simpleContent(project.getSimpleContent())
                .demoUrl(project.getDemoUrl())
                .projectMembers(project.getProjectMembers())
                .title(post.getTitle())
                .content(post.getContent())
                .tags(post.getTags())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .nickname(post.getUser().getNickname())
                .devcourseTrack(post.getUser().getDevcourseName().toString())
                .devcourseBatch(post.getUser().getDevcourseBatch());
    }

    public static ProjectResponseDTOBuilder jooqBuilder(Record record) {
        return ProjectResponseDTO.builder()
                .projectId(record.get(PROJECTS.ID))
                .postId(record.get(PROJECTS.POST_ID))
                .startedAt(record.get(PROJECTS.STARTED_AT))
                .endedAt(record.get(PROJECTS.ENDED_AT))
                .githubUrl(record.get(PROJECTS.GITHUB_URL))
                .simpleContent(record.get(PROJECTS.SIMPLE_CONTENT))
                .demoUrl(record.get(PROJECTS.DEMO_URL))
                .projectMembers(record.get(PROJECTS.PROJECT_MEMBERS, String.class))
                .title(record.get(POSTS.TITLE))
                .content(record.get(POSTS.CONTENT))
                .tags(record.get(POSTS.TAG))
//                .viewCount(record.get(POSTS.VIEW_COUNT))
                .likeCount(record.get(POSTS.LIKE_COUNT))
                .bookmarkCount(record.get("bookmarkCount", Integer.class))
                .createdAt(record.get(POSTS.CREATED_AT))
                .updatedAt(record.get(POSTS.UPDATED_AT))
                .nickname(record.get(USERS.NICKNAME))
                .devcourseTrack(record.get(USERS.DEVCOURSE_NAME, String.class))
                .devcourseBatch(record.get(USERS.DEVCOURSE_BATCH))
//                .thumbnailImageUrl(record.get("thumbnailImageUrl", String.class))
//                .hasImages(Boolean.TRUE.equals(record.get("hasImages", Boolean.class)))
                .hasBookmarked(Boolean.TRUE.equals(record.get("hasBookmarked", Boolean.class)))
                .hasLiked(Boolean.TRUE.equals(record.get("hasLiked", Boolean.class)));
    }
}
