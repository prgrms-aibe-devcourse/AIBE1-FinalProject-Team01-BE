package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

public class BoardAccessTestFixture {

    public static User createAdminUser() {
        return User.builder()
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();
    }

    public static User createStudentUser() {
        return User.builder()
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
    }

    public static User createGuestUser() {
        return User.builder()
                .email("guest@test.com")
                .role(Role.GUEST)
                .build();
    }

    public static Post createTestPost(BoardType boardType) {
        return Post.builder()
                .boardType(boardType)
                .build();
    }
}
