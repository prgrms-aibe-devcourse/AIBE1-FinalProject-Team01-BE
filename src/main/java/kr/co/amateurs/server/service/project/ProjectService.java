package kr.co.amateurs.server.service.project;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.post.PostRequest;
import kr.co.amateurs.server.domain.dto.project.ProjectMember;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.project.ProjectJooqRepository;
import kr.co.amateurs.server.repository.project.ProjectRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.file.FileService;
import kr.co.amateurs.server.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;

    private final ProjectJooqRepository projectJooqRepository;

    private final UserService userService;
    private final FileService fileService;

    private final JsonUtil jsonUtil;

    public PageResponseDTO<ProjectResponseDTO> getProjects(ProjectSearchParam params) {
        Page<ProjectResponseDTO> projects = userService.getCurrentUser()
                .map(user -> projectJooqRepository.findAllByUserId(params, user.getId()))
                .orElseGet(() -> projectJooqRepository.findAll(params));

        return PageResponseDTO.convertPageToDTO(projects);
    }

    public ProjectResponseDTO getProjectDetails(Long projectId) {
        return userService.getCurrentUser()
                .map(user -> projectJooqRepository.findByIdAndUserId(projectId, user.getId()))
                .orElseGet(() -> projectJooqRepository.findById(projectId));
    }

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO) {
        User user = userService.getCurrentLoginUser();

        // TODO: PostRequest DTO의 tags 가 String 이라서
        PostRequest postRequestDto = new CommunityRequestDTO(
                projectRequestDTO.title(),
                convertTagsToJSON(projectRequestDTO.tags()),
                projectRequestDTO.content()
        );

        Post post = Post.from(postRequestDto, user, BoardType.PROJECT_HUB);
        Post savedPost = postRepository.save(post);

        Project project = Project.builder()
                .post(post)
                .startedAt(projectRequestDTO.startedAt())
                .endedAt(projectRequestDTO.endedAt())
                .simpleContent(projectRequestDTO.simpleContent())
                .githubUrl(projectRequestDTO.githubUrl())
                .demoUrl(projectRequestDTO.demoUrl())
                .projectMembers(convertProjectMembersToJSON(projectRequestDTO.projectMembers()))
                .build();

        projectRepository.save(project);

        List<String> imgUrls = fileService.extractImageUrls(projectRequestDTO.content());
        fileService.savePostImage(savedPost, imgUrls);

        return ProjectResponseDTO.from(project);
    }

    @Transactional
    public void updateProject(Long projectId, ProjectRequestDTO projectRequestDTO) {
        User user = userService.getCurrentLoginUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        validatePost(project.getPost(), user.getEmail());

        CommunityRequestDTO postRequestDto = new CommunityRequestDTO(
                projectRequestDTO.title(),
                convertTagsToJSON(projectRequestDTO.tags()),
                projectRequestDTO.content()
        );

        Post post = project.getPost();

        post.update(postRequestDto);
        project.update(projectRequestDTO);
        project.updateProjectMembers(convertProjectMembersToJSON(projectRequestDTO.projectMembers()));
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User user = userService.getCurrentLoginUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        Post post = project.getPost();

        validatePost(post, user.getEmail());

        postRepository.delete(post);
    }

    private void validatePost(Post post, String email) {
        if (!post.getUser().getEmail().equals(email)) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    private String convertProjectMembersToJSON(List<ProjectMember> projectMembers) {
        return jsonUtil.listToJson(projectMembers);
    }

    private String convertTagsToJSON(List<String> tags) {
        return jsonUtil.listToJson(tags);
    }
}
