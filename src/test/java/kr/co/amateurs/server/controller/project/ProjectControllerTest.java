package kr.co.amateurs.server.controller.project;


import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.project.BookmarkFixture;
import kr.co.amateurs.server.fixture.project.PostFixture;
import kr.co.amateurs.server.fixture.project.ProjectFixture;
import kr.co.amateurs.server.fixture.project.UserFixture;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.project.ProjectRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProjectControllerTest extends AbstractControllerTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private Long backendPostId;
    private Long frontendPostId;
    private Long backendProjectId;
    private Long frontendProjectId;
    private String guestUserEmail;
    private String backendUserEmail;
    private String frontendUserEmail;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        cleanUpData();
        setUpData();
    }

    @Nested
    class 모든_유저는 {
        @Test
        void 필터링없이_목록조회를_요청하면_페이징된_목록이_반환되어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10))
                    .body("pageInfo.totalPages", equalTo(1))
                    .body("pageInfo.totalElements", equalTo(2));
        }

        @Test
        void 키워드로_목록조회를_요청하면_필터링된_목록이_반환되어야_한다() {
            // given
            String keyword = "백엔드";

            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("keyword", keyword)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10))
                    .body("pageInfo.totalPages", equalTo(1))
                    .body("pageInfo.totalElements", equalTo(1));
        }

        @Test
        void 데브코스_트랙으로_목록조회를_요청하면_필터링된_목록이_반환되어야_한다() {
            // given
            String course = DevCourseTrack.AI_BACKEND.toString();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("course", course)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10))
                    .body("pageInfo.totalPages", equalTo(1))
                    .body("pageInfo.totalElements", equalTo(1));
        }

        @Test
        void 트랙_기수로_목록조회를_요청하면_필터링된_목록이_반환되어야_한다() {
            // given
            String batch = "1";

            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("batch", batch)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10))
                    .body("pageInfo.totalPages", equalTo(1))
                    .body("pageInfo.totalElements", equalTo(1));
        }

        @Test
        void 모든_필터링_조건으로_목록조회를_요청하면_필터링된_목록이_반환되어야_한다() {
            // given
            String keyword = "프론트엔드";
            String course = DevCourseTrack.FRONTEND.toString();
            String batch = "5";

            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("keyword", keyword, "course", course, "batch", batch)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10))
                    .body("pageInfo.totalPages", equalTo(1))
                    .body("pageInfo.totalElements", equalTo(1));
        }

        @Test
        void 특정_프로젝트_조회를_요청하면_프로젝트_상세정보가_반환되어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .when()
                    .get("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("title", equalTo("백엔드 프로젝트"))
                    .body("content", equalTo("백엔드 설명"))
                    .body("nickname", startsWith("studentUser"))
                    .body("devcourseTrack", equalTo(DevCourseTrack.AI_BACKEND.toString()))
                    .body("devcourseBatch", equalTo("1"));
        }

        @Test
        void 음수로_된_페이지_번호로_목록조회를_요청하면_조회에_실패해야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("page", -1)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 최대_페이지_크기를_초과한_목록조회를_요청하면_조회에_실패해야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("size", 999)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 유효하지_않은_페이지_크기로_목록조회를_요청하면_조회에_실패해야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .params("size", 0)
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class 게스트_유저는 {
        private String fakeGuestUserToken() {
            return jwtProvider.generateAccessToken(guestUserEmail);
        }

        @Test
        void 프로젝트_생성_요청을_하면_생성을_할_수_없어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeGuestUserToken())
                    .when()
                    .post("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        void 프로젝트_수정_요청을_하면_수정을_할_수_없어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeGuestUserToken())
                    .when()
                    .put("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        void 프로젝트_삭제_요청을_하면_삭제를_할_수_없어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeGuestUserToken())
                    .when()
                    .delete("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }



    @Nested
    class 학생_유저는 {
        private String fakeBackendUserToken() {
            return jwtProvider.generateAccessToken(backendUserEmail);
        }

        @Test
        void 프로젝트_목록조회를_요청하면_게시글의_메타정보를_포함해서_반환되어야_한다() {
            // given
            ProjectSearchParam projectSearchParam = ProjectSearchParam.builder()
                    .keyword("프론트엔드")
                    .course(DevCourseTrack.FRONTEND)
                    .field(PaginationSortType.POST_LATEST)
                    .batch("5")
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .params("keyword", projectSearchParam.getKeyword(), "course", projectSearchParam.getCourse().toString(), "batch", projectSearchParam.getBatch())
                    .when()
                    .get("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].hasLiked", notNullValue())
                    .body("content[0].hasBookmarked", notNullValue())
                    .body("content[0].bookmarkCount", notNullValue())
                    .body("content[0].likeCount", notNullValue());
        }

        @Test
        void 프로젝트_상세조회를_요청하면_게시글의_메타정보를_포함해서_반환되어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .when()
                    .get("/projects/%s".formatted(frontendProjectId))
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.OK.value())
                    .body("hasLiked", notNullValue())
                    .body("hasBookmarked", notNullValue())
                    .body("bookmarkCount", notNullValue())
                    .body("likeCount", notNullValue());
        }

        @Test
        void 프로젝트_생성_요청을_하면_새로운_게시글이_생성되어야_한다() throws JsonProcessingException {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("새로운 프로젝트")
                    .content("프로젝트 내용")
                    .startedAt(LocalDateTime.of(2025, 1, 1, 13, 20))
                    .endedAt(LocalDateTime.of(2025, 1, 31, 13, 20))
                    .githubUrl("https://github.com/example/project")
                    .simpleContent("간단한 프로젝트 설명")
                    .demoUrl("https://demo.example.com")
                    .projectMembers(objectMapper.writeValueAsString(List.of("김개발", "이디자인", "박프론트")))
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .body(requestDTO)
                    .when()
                    .post("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.CREATED.value())
                    .body("projectId", notNullValue())
                    .body("postId", notNullValue())
                    .body("title", equalTo("새로운 프로젝트"))
                    .body("content", equalTo("프로젝트 내용"));
        }

        @Test
        void 프로젝트_수정_요청을_하면_게시글이_수정되어야_한다() throws JsonProcessingException {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트")
                    .content("수정된 내용")
                    .startedAt(LocalDateTime.of(2020, 1, 1, 13, 20))
                    .endedAt(LocalDateTime.of(2020, 1, 31, 13, 20))
                    .projectMembers(objectMapper.writeValueAsString(List.of("홍길동", "박길동", "김길동")))
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .body(requestDTO)
                    .when()
                    .put("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 프로젝트_삭제_요청을_하면_게시글이_삭제되어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .when()
                    .delete("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 프로젝트_수정_요청을_본인이_아니라면_할_수_없어야_한다() throws JsonProcessingException {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트2")
                    .content("수정된 내용2")
                    .projectMembers(objectMapper.writeValueAsString(List.of("홍길동", "박길동", "김길동")))
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .body(requestDTO)
                    .when()
                    .put("/projects/{projectId}", frontendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        void 프로젝트_삭제_요청을_본인이_아니라면_할_수_없어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .header("Authorization", "Bearer " + fakeBackendUserToken())
                    .when()
                    .delete("/projects/{projectId}", frontendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class 비로그인_유저는 {
        @Test
        void 프로젝트_생성_요청을_하면_생성을_할_수_없어야_한다() throws JsonProcessingException {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("새로운 프로젝트")
                    .content("프로젝트 내용")
                    .startedAt(LocalDateTime.of(2025, 1, 1, 13, 20))
                    .endedAt(LocalDateTime.of(2025, 1, 31, 13, 20))
                    .githubUrl("https://github.com/example/project")
                    .simpleContent("간단한 프로젝트 설명")
                    .demoUrl("https://demo.example.com")
                    .projectMembers(objectMapper.writeValueAsString(List.of("김개발", "이디자인", "박프론트")))
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .body(requestDTO)
                    .when()
                    .post("/projects")
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 프로젝트_수정_요청을_하면_수정을_할_수_없어야_한다() {
            // given
            ProjectRequestDTO requestDTO = ProjectRequestDTO.builder()
                    .title("수정된 프로젝트")
                    .content("수정된 내용")
                    .startedAt(LocalDateTime.of(2020, 1, 1, 13, 20))
                    .endedAt(LocalDateTime.of(2020, 1, 31, 13, 20))
                    .projectMembers("[\"홍길동\", \"박길동\", \"김길동\"]")
                    .build();

            // when & then
            given()
                    .header("content-type", "application/json")
                    .body(requestDTO)
                    .when()
                    .put("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 프로젝트_삭제_요청을_하면_삭제를_할_수_없어야_한다() {
            // when & then
            given()
                    .header("content-type", "application/json")
                    .when()
                    .delete("/projects/{projectId}", backendProjectId)
                    .then()
                    .log().all()
                    .time(lessThan(2000L))
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    private void setUpData() throws JsonProcessingException {
        createUsers();
        createPosts();
        createProjects();
        createBookmarks();
    }

    private void createUsers() {
        User guestUser = UserFixture.createGuestUser();
        User backendUser = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "1");
        User frontendUser = UserFixture.createStudentUser(DevCourseTrack.FRONTEND, "5");

        List<User> savedUsers = userRepository.saveAll(List.of(guestUser, backendUser, frontendUser));

        this.guestUserEmail = savedUsers.get(0).getEmail();
        this.backendUserEmail = savedUsers.get(1).getEmail();
        this.frontendUserEmail = savedUsers.get(2).getEmail();
    }

    private void createPosts() {
        User backendUser = userRepository.findByEmail(backendUserEmail).orElseThrow();
        User frontendUser = userRepository.findByEmail(frontendUserEmail).orElseThrow();

        Post backendPost = PostFixture.createPost(backendUser, "백엔드 프로젝트", "백엔드 설명", BoardType.PROJECT_HUB);
        Post frontendPost = PostFixture.createPost(frontendUser, "프론트엔드 프로젝트", "프론트엔드 설명", BoardType.PROJECT_HUB);

        List<Post> savedPosts = postRepository.saveAll(List.of(backendPost, frontendPost));

        this.backendPostId = savedPosts.get(0).getId();
        this.frontendPostId = savedPosts.get(1).getId();
    }

    private void createProjects() throws JsonProcessingException {
        Post backendPost = postRepository.findById(backendPostId).orElseThrow();
        Post frontendPost = postRepository.findById(frontendPostId).orElseThrow();

        List<String> members = List.of("김개발", "이디자인", "박프론트");

        Project backendProject = ProjectFixture.createAiBackendProject(
                backendPost,
                LocalDateTime.of(2025, 1, 1, 13, 20),
                LocalDateTime.of(2025, 1, 31, 13, 20),
                members
        );

        Project frontendProject = ProjectFixture.createFrontendProject(
                frontendPost,
                LocalDateTime.of(2025, 2, 1, 13, 20),
                LocalDateTime.of(2025, 2, 28, 13, 20),
                members
        );

        List<Project> savedProjects = projectRepository.saveAll(List.of(backendProject, frontendProject));

        this.backendProjectId = savedProjects.get(0).getId();
        this.frontendProjectId = savedProjects.get(1).getId();
    }

    private void createBookmarks() {
        User backendUser = userRepository.findByEmail(backendUserEmail).orElseThrow();
        Post backendPost = postRepository.findById(backendPostId).orElseThrow();
        Post frontendPost = postRepository.findById(frontendPostId).orElseThrow();

        Bookmark backendBookmark = BookmarkFixture.createBookmark(backendUser, backendPost);
        Bookmark frontendBookmark = BookmarkFixture.createBookmark(backendUser, frontendPost);
        bookmarkRepository.save(backendBookmark);
        bookmarkRepository.save(frontendBookmark);
    }

    private void cleanUpData() {
        bookmarkRepository.deleteAll();
        projectRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}