package kr.co.amateurs.server.service.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.project.ProjectMember;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.project.BookmarkFixture;
import kr.co.amateurs.server.fixture.project.PostFixture;
import kr.co.amateurs.server.fixture.project.ProjectFixture;
import kr.co.amateurs.server.fixture.project.UserFixture;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.project.ProjectRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private User backendUser;
    private User frontendUser;
    private User guestUser;
    private Post backendPost;
    private Post frontendPost;
    private Project backendProject;
    private Project frontendProject;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        cleanUpData();
        setUpData();
    }

    @Nested
    class 프로젝트_목록_조회를 {
        @Test
        void 필터링없이_조회하면_페이징된_프로젝트_목록이_반환되어야_한다() {
            // given
            ProjectSearchParam searchParam = new ProjectSearchParam();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
            assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        }

        @Test
        void 키워드로_필터링하면_키워드와_일치하는_프로젝트_목록이_반환되어야_한다() {
            // given
            ProjectSearchParam searchParam = ProjectSearchParam.builder()
                    .keyword("백엔드")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .field(PaginationSortType.ID)
                    .build();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).contains("백엔드");
        }

        @Test
        void 데브코스_트랙으로_필터링하면_트랙과_일치하는_프로젝트_목록이_반환되어야_한다() {
            // given
            ProjectSearchParam searchParam = ProjectSearchParam.builder()
                    .course(DevCourseTrack.AI_BACKEND)
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .field(PaginationSortType.ID)
                    .build();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).devcourseTrack()).isEqualTo(DevCourseTrack.AI_BACKEND.toString());
        }

        @Test
        void 기수로_필터링하면_기수와_일치하는_프로젝트_목록이_반환되어야_한다() {
            // given
            ProjectSearchParam searchParam = ProjectSearchParam.builder()
                    .batch("1")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .field(PaginationSortType.ID)
                    .build();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).devcourseBatch()).isEqualTo("1");
        }

        @Test
        void 모든_필터링_조건으로_검색하면_조건과_일치하는_프로젝트_목록이_반환되어야_한다() {
            // given
            ProjectSearchParam searchParam = ProjectSearchParam.builder()
                    .keyword("프론트엔드")
                    .course(DevCourseTrack.FRONTEND)
                    .batch("5")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .field(PaginationSortType.ID)
                    .build();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).contains("프론트엔드");
            assertThat(result.content().get(0).devcourseTrack()).isEqualTo(DevCourseTrack.FRONTEND.toString());
            assertThat(result.content().get(0).devcourseBatch()).isEqualTo("5");
        }

        @Test
        void 북마크_수가_모두_포함되게_반환되어야_한다() {
            // given
            Bookmark bookmark = BookmarkFixture.createBookmark(backendUser, backendPost);
            bookmarkRepository.save(bookmark);

            ProjectSearchParam searchParam = new ProjectSearchParam();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            PageResponseDTO<ProjectResponseDTO> result = projectService.getProjects(searchParam);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().stream()
                    .filter(p -> p.postId().equals(backendPost.getId()))
                    .findFirst()
                    .orElseThrow()
                    .bookmarkCount()
            ).isEqualTo(1);
        }
    }

    @Nested
    class 프로젝트_상세_정보를 {
        @Test
        void 존재하는_프로젝트는_정보가_반환되어야_한다() {
            // given
            Long projectId = backendProject.getId();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when
            ProjectResponseDTO result = projectService.getProjectDetails(projectId);

            // then
            assertThat(result.projectId()).isEqualTo(backendProject.getId());
            assertThat(result.postId()).isEqualTo(backendPost.getId());
            assertThat(result.title()).isEqualTo("백엔드 프로젝트");
            assertThat(result.content()).isEqualTo("백엔드 설명");
            assertThat(result.nickname()).isEqualTo(backendUser.getNickname());
            assertThat(result.devcourseTrack()).isEqualTo(DevCourseTrack.AI_BACKEND.toString());
            assertThat(result.devcourseBatch()).isEqualTo("1");
            assertThat(result.githubUrl()).isEqualTo("https://github.com/example/ai-backend-project");
            assertThat(result.simpleContent()).isEqualTo("AI 백엔드 프로젝트");
        }

        @Test
        void 존재하지_않는_프로젝트는_조회하면_예외가_발생해야_한다() {
            // given
            Long projectId = 999L;
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when & then
            assertThatThrownBy(() -> projectService.getProjectDetails(projectId))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 북마크_수가_포함되게_반환해야_한다() {
            // given
            Bookmark bookmark1 = BookmarkFixture.createBookmark(backendUser, backendPost);
            Bookmark bookmark2 = BookmarkFixture.createBookmark(frontendUser, backendPost);
            bookmarkRepository.saveAll(List.of(bookmark1, bookmark2));
            when(userService.getCurrentLoginUser()).thenReturn(backendUser);

            // when
            ProjectResponseDTO result = projectService.getProjectDetails(backendProject.getId());

            // then
            assertThat(result.bookmarkCount()).isEqualTo(2);
        }
    }

    @Nested
    class 프로젝트_생성을 {
        @Test
        void 유효한_데이터로_요청하면_프로젝트가_저장되어야_한다() throws JsonProcessingException {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("새로운 프로젝트")
                    .content("프로젝트 내용")
                    .tags(List.of("스프링", "초보환영"))
                    .startedAt(LocalDateTime.of(2025, 1, 1, 13, 20))
                    .endedAt(LocalDateTime.of(2025, 1, 31, 13, 20))
                    .githubUrl("https://github.com/example/new-project")
                    .simpleContent("간단한 설명")
                    .demoUrl("https://demo.example.com")
                    .projectMembers(List.of(
                            new ProjectMember("김개발", "백엔드"),
                            new ProjectMember("박개발", "프론트엔드"),
                            new ProjectMember("홍길동", "DevOps")
                    ))
                    .build();

            when(userService.getCurrentLoginUser()).thenReturn(backendUser);

            // when
            ProjectResponseDTO result = projectService.createProject(requestDTO);

            // then
            assertThat(result.projectId()).isNotNull();
            assertThat(result.postId()).isNotNull();
            assertThat(result.title()).isEqualTo("새로운 프로젝트");
            assertThat(result.content()).isEqualTo("프로젝트 내용");
            assertThat(result.tags()).isEqualTo("[\"스프링\",\"초보환영\"]");
            assertThat(result.githubUrl()).isEqualTo("https://github.com/example/new-project");
            assertThat(result.simpleContent()).isEqualTo("간단한 설명");
            assertThat(result.demoUrl()).isEqualTo("https://demo.example.com");
        }

        @Test
        void 존재하지_않는_사용자로_프로젝트_생성하면_예외가_발생해야_한다() {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("새로운 프로젝트")
                    .content("프로젝트 내용")
                    .build();

            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when & then
            assertThatThrownBy(() -> projectService.createProject(requestDTO))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 프로젝트_수정을 {
        @Test
        void 존재하는_프로젝트는_본인이라면_수정되어야_한다() throws JsonProcessingException {
            // given
            Long projectId = backendProject.getId();
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트")
                    .content("수정된 내용")
                    .startedAt(LocalDateTime.of(2024, 6, 1, 10, 0))
                    .endedAt(LocalDateTime.of(2024, 6, 30, 18, 0))
                    .githubUrl("https://github.com/example/updated-project")
                    .simpleContent("수정된 심플 소개 쏼라쏼라")
                    .demoUrl("https://updated-demo.example.com")
                    .projectMembers(List.of(
                            new ProjectMember("김개발", "백엔드"),
                            new ProjectMember("박개발", "프론트엔드"),
                            new ProjectMember("홍길동", "DevOps")
                    ))
                    .build();
            when(userService.getCurrentLoginUser()).thenReturn(backendUser);

            // when
            projectService.updateProject(projectId, requestDTO);
            projectRepository.flush();

            // then
            ProjectResponseDTO updatedProjectResponseDTO = projectService.getProjectDetails(projectId);

            assertThat(updatedProjectResponseDTO.title()).isEqualTo("수정된 프로젝트");
            assertThat(updatedProjectResponseDTO.content()).isEqualTo("수정된 내용");
            assertThat(updatedProjectResponseDTO.startedAt()).isEqualTo(LocalDateTime.of(2024, 6, 1, 10, 0));
            assertThat(updatedProjectResponseDTO.endedAt()).isEqualTo(LocalDateTime.of(2024, 6, 30, 18, 0));
            assertThat(updatedProjectResponseDTO.githubUrl()).isEqualTo("https://github.com/example/updated-project");
            assertThat(updatedProjectResponseDTO.simpleContent()).isEqualTo("수정된 심플 소개 쏼라쏼라");
            assertThat(updatedProjectResponseDTO.demoUrl()).isEqualTo("https://updated-demo.example.com");
        }

        @Test
        void 본인이_아닌_다른_유저가_수정하면_예외가_발생해야_한다() {
            // given
            Long projectId = backendProject.getId();
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트")
                    .content("수정된 내용")
                    .build();
            when(userService.getCurrentLoginUser()).thenReturn(frontendUser);

            // when & then
            assertThatThrownBy(() -> projectService.updateProject(projectId, requestDTO))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_프로젝트는_예외가_발생해야_한다() {
            // given
            Long projectId = 999L;
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트")
                    .content("수정된 내용")
                    .build();

            // when & then
            assertThatThrownBy(() -> projectService.updateProject(projectId, requestDTO))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 일부_내용만_수정하면_해당_내용만_수정되어야_한다() {
            // given
            Long projectId = backendProject.getId();
            String originalGithubUrl = backendProject.getGithubUrl();
            String originalSimpleContent = backendProject.getSimpleContent();

            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("제목 수정 어쩌구저쩌구")
                    .content("내용수정 저쩌구어쩌구")
                    .startedAt(LocalDateTime.of(2024, 12, 1, 9, 0))
                    .endedAt(LocalDateTime.of(2024, 12, 31, 18, 0))
                    .build();

            when(userService.getCurrentLoginUser()).thenReturn(backendUser);

            // when
            projectService.updateProject(projectId, requestDTO);
            projectRepository.flush();

            // then
            ProjectResponseDTO updatedProjectResponseDTO = projectService.getProjectDetails(projectId);

            assertThat(updatedProjectResponseDTO.title()).isEqualTo("제목 수정 어쩌구저쩌구");
            assertThat(updatedProjectResponseDTO.content()).isEqualTo("내용수정 저쩌구어쩌구");
            assertThat(updatedProjectResponseDTO.startedAt()).isEqualTo(requestDTO.startedAt());
            assertThat(updatedProjectResponseDTO.endedAt()).isEqualTo(requestDTO.endedAt());
            assertThat(updatedProjectResponseDTO.githubUrl()).isEqualTo(originalGithubUrl);
            assertThat(updatedProjectResponseDTO.simpleContent()).isEqualTo(originalSimpleContent);
        }
    }

    @Nested
    class 프로젝트_삭제를 {
        @Test
        void 존재하는_프로젝트는_본인이라면_삭제되어야_한다() {
            // given
            Long projectId = backendProject.getId();
            when(userService.getCurrentLoginUser()).thenReturn(backendUser);

            // when
            projectService.deleteProject(projectId);

            // then
            assertThat(projectRepository.findById(projectId)).isEmpty();
        }

        @Test
        void 존재하지_않는_프로젝트는_삭제하면_예외가_발생해야_한다() {
            // given
            Long projectId = 999L;

            // when & then
            assertThatThrownBy(() -> projectService.deleteProject(projectId))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_사용자가_삭제하면_예외가_발생해야_한다() {
            // given
            Long projectId = backendProject.getId();
            when(userService.getCurrentLoginUser()).thenThrow(new CustomException(ErrorCode.ANONYMOUS_USER));

            // when & then
            assertThatThrownBy(() -> projectService.deleteProject(projectId))
                    .isInstanceOf(CustomException.class);
        }
    }

    private void setUpData() throws JsonProcessingException {
        createUsers();
        createPosts();
        createProjects();
    }

    private void cleanUpData() {
        bookmarkRepository.deleteAll();
        projectRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createUsers() {
        guestUser = UserFixture.createGuestUser();
        backendUser = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "1");
        frontendUser = UserFixture.createStudentUser(DevCourseTrack.FRONTEND, "5");

        List<User> savedUsers = userRepository.saveAll(List.of(guestUser, backendUser, frontendUser));
        this.guestUser = savedUsers.get(0);
        this.backendUser = savedUsers.get(1);
        this.frontendUser = savedUsers.get(2);
    }

    private void createPosts() {
        backendPost = PostFixture.createPost(backendUser, "백엔드 프로젝트", "백엔드 설명", BoardType.PROJECT_HUB);
        frontendPost = PostFixture.createPost(frontendUser, "프론트엔드 프로젝트", "프론트엔드 설명", BoardType.PROJECT_HUB);

        List<Post> savedPosts = postRepository.saveAll(List.of(backendPost, frontendPost));
        this.backendPost = savedPosts.get(0);
        this.frontendPost = savedPosts.get(1);
    }

    private void createProjects() throws JsonProcessingException {
        List<String> members = List.of("김개발", "이디자인", "박프론트");

        backendProject = ProjectFixture.createAiBackendProject(
                backendPost,
                LocalDateTime.of(2025, 1, 1, 13, 20),
                LocalDateTime.of(2025, 1, 31, 13, 20),
                members
        );

        frontendProject = ProjectFixture.createFrontendProject(
                frontendPost,
                LocalDateTime.of(2025, 2, 1, 13, 20),
                LocalDateTime.of(2025, 2, 28, 13, 20),
                members
        );

        List<Project> savedProjects = projectRepository.saveAll(List.of(backendProject, frontendProject));
        this.backendProject = savedProjects.get(0);
        this.frontendProject = savedProjects.get(1);
    }
}
