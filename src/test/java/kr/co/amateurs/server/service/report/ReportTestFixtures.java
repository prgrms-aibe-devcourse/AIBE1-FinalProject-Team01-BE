package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
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
                .viewCount(10)
                .likeCount(5)
                .comments(new ArrayList<>())
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
                .viewCount(0)
                .likeCount(0)
                .comments(new ArrayList<>())
                .postImages(new ArrayList<>())
                .build();
    }

    public static Comment createTestComment(Post post, User user) {
        return Comment.builder()
                .post(post)
                .user(user)
                .content("테스트 댓글")
                .build();
    }

    public static Comment createCustomComment(Post post, User user, String content) {
        return Comment.builder()
                .post(post)
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
                .build();
    }

    public static Report createCommentReport(User reporter, Comment comment, String description) {
        return Report.builder()
                .user(reporter)
                .comment(comment)
                .description(description)
                .status(ReportStatus.PENDING)
                .build();
    }

    public static Report createReportWithStatus(User reporter, Post post, String description, ReportStatus status) {
        return Report.builder()
                .user(reporter)
                .post(post)
                .description(description)
                .status(status)
                .build();
    }

    public static ReportRequestDTO createPostReportRequestDTO(Long postId, String description) {
        return new ReportRequestDTO(
                postId,
                ReportType.POST,
                description
        );
    }

    public static ReportRequestDTO createCommentReportRequestDTO(Long commentId, String description) {
        return new ReportRequestDTO(
                commentId,
                ReportType.COMMENT,
                description
        );
    }
}
