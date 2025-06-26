package kr.co.amateurs.server.service.project;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkCount;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectPageResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.project.ProjectRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final UserService userService;

    public ProjectPageResponseDTO getProjects(ProjectSearchParam params) {
        Page<Project> projects = projectRepository.findAllByFilterOptionsOrderByLikeCountDesc(
                params.getKeyword(),
                params.getCourse(),
                params.getBatch(),
                params.toPageable()
        );

        List<Long> postIds = projects.getContent().stream()
                .map(project -> project.getPost().getId())
                .toList();

        List<BookmarkCount> bookmarkCounts = bookmarkRepository.countByPostIds(postIds);

        return ProjectPageResponseDTO.from(projects, bookmarkCounts);
    }

    public ProjectResponseDTO getProjectDetails(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        int bookmarkCount = bookmarkRepository.countByPostId(project.getPost().getId());

        return ProjectResponseDTO.from(project, bookmarkCount);
    }

    @Transactional
    public ProjectResponseDTO createProject(String username, ProjectRequestDTO projectRequestDTO) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        CommunityRequestDTO postRequestDto = new CommunityRequestDTO(
                projectRequestDTO.title(),
                projectRequestDTO.tags(),
                projectRequestDTO.content()
        );

        Post post = Post.from(postRequestDto, user, BoardType.PROJECT_HUB);
        postRepository.save(post);

        Project project = Project.builder()
                .post(post)
                .startedAt(projectRequestDTO.startedAt())
                .endedAt(projectRequestDTO.endedAt())
                .simpleContent(projectRequestDTO.simpleContent())
                .githubUrl(projectRequestDTO.githubUrl())
                .demoUrl(projectRequestDTO.demoUrl())
                .projectMembers(projectRequestDTO.projectMembers())
                .build();

        projectRepository.save(project);

        return ProjectResponseDTO.from(project);
    }

    @Transactional
    public void updateProject(String username, Long projectId, ProjectRequestDTO projectRequestDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        validatePost(project.getPost(), username);

        CommunityRequestDTO postRequestDto = new CommunityRequestDTO(
                projectRequestDTO.title(),
                projectRequestDTO.tags(),
                projectRequestDTO.content()
        );

        Post post = project.getPost();

        post.update(postRequestDto);
        project.update(projectRequestDTO);
    }

    // TODO: Soft delete로 변경 예정
    @Transactional
    public void deleteProject(String username, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        validatePost(project.getPost(), username);

        projectRepository.deleteById(projectId);
    }

    private void validatePost(Post post, String username) {
        if (!post.getUser().getEmail().equals(username)) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }
}
