package kr.co.amateurs.server.fixture.it;

import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.ITPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostImage;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

import java.util.ArrayList;

public class ITTestFixtures {
    public static User createAdminUser() {
        return User.builder()
                .email("admin@test.com")
                .nickname("admin")
                .name("관리자")
                .role(Role.ADMIN)
                .build();
    }

    public static User createStudentUser() {
        return User.builder()
                .email("student@test.com")
                .nickname("student")
                .name("수강생")
                .role(Role.STUDENT)
                .build();
    }

    public static User createGuestUser() {
        return User.builder()
                .email("guest@test.com")
                .nickname("guest")
                .name("게스트")
                .role(Role.GUEST)
                .build();
    }

    public static User createCustomUser(String email, String nickname, String name, Role role) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .name(name)
                .role(role)
                .build();
    }

    public static Post createPost(User user, String title, String content, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags("태그1, 태그2")
                .boardType(boardType)
                .viewCount(0)
                .likeCount(0)
                .comments(new ArrayList<>())
                .postImages(new ArrayList<>())
                .build();
    }

    public static Post createPostWithCounts(User user, String title, String content,
                                            BoardType boardType, int viewCount, int likeCount) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags("테스트,태그")
                .boardType(boardType)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .comments(new ArrayList<>())
                .postImages(new ArrayList<>())
                .build();
    }

    // ITPost 생성 메서드 추가
    public static ITPost createITPost(Post post) {
        return ITPost.builder()
                .post(post)
                .build();
    }

    // Post + ITPost를 함께 생성하는 편의 메서드
    public static ITPost createITPostWithPost(User user, String title, String content, BoardType boardType) {
        Post post = createPost(user, title, content, boardType);
        return createITPost(post);
    }

    public static ITPost createITPostWithPostAndCounts(User user, String title, String content,
                                                       BoardType boardType, int viewCount, int likeCount) {
        Post post = createPostWithCounts(user, title, content, boardType, viewCount, likeCount);
        return createITPost(post);
    }

    public static Comment createComment(Post post, User user, String content) {
        return Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();
    }

    public static PostImage createPostImage(Post post, String imageUrl) {
        return PostImage.builder()
                .post(post)
                .imageUrl(imageUrl)
                .build();
    }

    public static ITRequestDTO createRequestDTO(String title, String tags, String content) {
        return new ITRequestDTO(title, tags, content);
    }
}
