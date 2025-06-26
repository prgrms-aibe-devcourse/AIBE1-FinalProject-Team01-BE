package kr.co.amateurs.server.domain.dto.project;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkCount;
import kr.co.amateurs.server.domain.dto.common.PageInfo;
import kr.co.amateurs.server.domain.entity.post.Project;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record ProjectPageResponseDTO(
        List<ProjectResponseDTO> projects,
        PageInfo pageInfo
) {
    public static ProjectPageResponseDTO from(Page<Project> projects, List<BookmarkCount> bookmarkCounts) {
        List<ProjectResponseDTO> projectDTOs = projects.stream()
                .map(project -> {
                    Long postId = project.getPost().getId();
                    int bookmarkCount = bookmarkCounts.stream()
                            .filter(bc -> bc.getPostId().equals(postId))
                            .map(BookmarkCount::getBookmarkCount)
                            .findFirst()
                            .orElse(0);

                    return ProjectResponseDTO.from(project, bookmarkCount);
                })
                .toList();

        return ProjectPageResponseDTO.builder()
                .projects(projectDTOs)
                .pageInfo(PageInfo.from(projects))
                .build();
    }
}
