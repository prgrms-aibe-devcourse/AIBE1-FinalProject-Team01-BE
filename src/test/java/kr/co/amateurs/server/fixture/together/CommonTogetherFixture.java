package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.springframework.data.domain.Sort;

public class CommonTogetherFixture {
    public static Post createPost(User user, String title, String content, String tags, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags(tags)
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .boardType(boardType)
                .build();
    }

    public static User createStudent() {
        return User.builder()
                .role(Role.STUDENT)
                .devcourseName(DevCourseTrack.AI_BACKEND)
                .devcourseBatch("1기")
                .email("student@test.com")
                .name("student_name")
                .nickname("student_nickname")
                .build();
    }

    public static User createAdmin() {
        return User.builder()
                .role(Role.ADMIN)
                .email("admin@test.com")
                .name("admin_name")
                .nickname("admin_nickname")
                .build();
    }

    public static User createGuest() {
        return User.builder()
                .role(Role.GUEST)
                .email("guest@test.com")
                .name("guest_name")
                .nickname("guest_nickname")
                .build();
    }

    public static String fakeStudentToken() {
        return "fake-student-token";
    }

    public static String fakeAdminToken() {
        return "fake-admin-token";
    }

    public static String fakeGuestToken() { return "fake-guest-token"; }

    public static PostPaginationParam createPaginationParam(String keyword, PaginationSortType sortType) {
        return PostPaginationParam.builder()
                .keyword(keyword)
                .page(0)
                .size(10)
                .field(sortType)
                .sortDirection(Sort.Direction.DESC)
                .build();
    }
}
