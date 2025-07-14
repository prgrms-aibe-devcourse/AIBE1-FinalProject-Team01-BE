package kr.co.amateurs.server.fixture.report;

import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

import java.util.ArrayList;

public class ReportTestFixtures {

    public static User createTestUser() {
        return User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .password("testPassword")
                .role(Role.STUDENT)
                .name("testName")
                .build();
    }

    public static User createGuestUser() {
        return User.builder()
                .nickname("guestUser")
                .email("guest@test.com")
                .password("Password")
                .role(Role.GUEST)
                .name("guestName")
                .build();
    }

    public static User createStudentUser() {
        return User.builder()
                .nickname("student")
                .email("student@test.com")
                .password("Password")
                .role(Role.STUDENT)
                .name("신고자")
                .build();
    }

    public static User createAdminUser() {
        return User.builder()
                .nickname("admin")
                .email("admin@test.com")
                .password("adminPassword")
                .role(Role.ADMIN)
                .name("관리자")
                .build();
    }

    public static Post createTestPost(User user) {
        return Post.builder()
                .user(user)
                .title("테스트 제목")
                .content("테스트 내용")
                .tags("테스트태그")
                .boardType(BoardType.FREE)
                .likeCount(5)
                .postImages(new ArrayList<>())
                .build();
    }

    public static Post createCustomPost(User user, String title, String content, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags("태그1,태그2")
                .boardType(boardType)
                .likeCount(0)
                .postImages(new ArrayList<>())
                .build();
    }

    public static Comment createTestComment(Post post, User user) {
        return Comment.builder()
                .postId(post.getId())
                .user(user)
                .content("테스트 댓글")
                .build();
    }

    public static Comment createCustomComment(Post post, User user, String content) {
        return Comment.builder()
                .postId(post.getId())
                .user(user)
                .content(content)
                .build();
    }

    public static Report createPostReport(User reporter, Post post, String description) {
        return Report.builder()
                .user(reporter)
                .post(post)
                .description(description)
                .status(ReportStatus.PENDING)
                .reportType(ReportType.BAD_WORDS)
                .reportTarget(ReportTarget.POST)
                .build();
    }

    public static Report createCommentReport(User reporter, Comment comment, String description) {
        return Report.builder()
                .user(reporter)
                .comment(comment)
                .description(description)
                .status(ReportStatus.PENDING)
                .reportType(ReportType.BAD_WORDS)
                .reportTarget(ReportTarget.COMMENT)
                .build();
    }

    public static Report createReportWithStatus(User reporter, Post post, String description, ReportStatus status) {
        return Report.builder()
                .user(reporter)
                .post(post)
                .description(description)
                .status(status)
                .reportType(ReportType.BAD_WORDS)
                .reportTarget(ReportTarget.POST)
                .build();
    }

    public static ReportRequestDTO createPostReportRequestDTO(Long postId, String description) {
        return new ReportRequestDTO(
                postId,
                ReportTarget.POST,
                description,
                ReportType.BAD_WORDS
        );
    }

    public static ReportRequestDTO createCommentReportRequestDTO(Long commentId, String description) {
        return new ReportRequestDTO(
                commentId,
                ReportTarget.COMMENT,
                description,
                ReportType.BAD_WORDS
        );
    }

    public static Post createNormalPost(User user) {
        return Post.builder()
                .user(user)
                .title("유익한 정보 공유 게시글")
                .content("안녕하세요. 오늘은 Spring Boot의 테스트 작성 방법에 대해 공유하고자 합니다. " +
                        "좋은 테스트를 작성하면 코드 품질을 높일 수 있고, 리팩토링 시에도 안전합니다. " +
                        "여러분도 테스트 작성 습관을 길러보시기 바랍니다.")
                .tags("개발,공유,교육")
                .boardType(BoardType.FREE)
                .likeCount(5)
                .postImages(new ArrayList<>())
                .build();
    }

    public static Post createViolationPost(User user) {
        return Post.builder()
                .user(user)
                .title("부적절한 제목입니다")
                .content("이 게시글에는 욕설과 혐오 표현이 포함되어 있습니다. " +
                        "실제로는 부적절한 내용이라고 가정하고 AI가 감지할 수 있도록 " +
                        "키워드를 포함시킨 테스트용 게시글입니다.")
                .tags("위반,테스트")
                .boardType(BoardType.FREE)
                .likeCount(0)
                .postImages(new ArrayList<>())
                .build();
    }

    public static ReportRequestDTO createSpamReportRequestDTO(Long targetId, ReportTarget target) {
        return new ReportRequestDTO(
                targetId,
                target,
                "동일한 내용을 반복적으로 게시하는 스팸입니다",
                ReportType.SPAM
        );
    }

    public static ReportRequestDTO createPrivacyReportRequestDTO(Long targetId, ReportTarget target) {
        return new ReportRequestDTO(
                targetId,
                target,
                "개인정보가 무단으로 노출되었습니다",
                ReportType.PERSONAL_INFO
        );
    }

    public static ReportRequestDTO createViolenceReportRequestDTO(Long targetId, ReportTarget target) {
        return new ReportRequestDTO(
                targetId,
                target,
                "폭력적이고 위험한 내용이 포함되어 있습니다",
                ReportType.BAD_WORDS
        );
    }
}
