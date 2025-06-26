package kr.co.amateurs.server.fixture.project;

import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

public class UserFixture {
    private static int count = 0;

    public static User createAnonymousUser() {
        return User.builder()
                .email("test_%s@example.com".formatted(count++))
                .name("김테스터")
                .nickname("testUser_%s".formatted(count))
                .role(Role.ANONYMOUS)
                .build();
    }

    public static User createGuestUser() {
        return User.builder()
                .email("guest_%s@example.com".formatted(count++))
                .name("김게스트")
                .nickname("guestUser_%s".formatted(count))
                .role(Role.GUEST)
                .build();
    }

    public static User createStudentUser(DevCourseTrack devCourseTrack, String devCourseBatch) {
        return User.builder()
                .email("student_%s@example.com".formatted(count++))
                .name("박수강생")
                .nickname("studentUser_%s".formatted(count))
                .role(Role.STUDENT)
                .devcourseName(devCourseTrack)
                .devcourseBatch(devCourseBatch)
                .build();
    }

    public static User createAdminUser() {
        return User.builder()
                .email("admin_%s@example.com".formatted(count++))
                .nickname("adminUser_%s".formatted(count))
                .name("이관리자")
                .role(Role.ADMIN)
                .build();
    }
}
