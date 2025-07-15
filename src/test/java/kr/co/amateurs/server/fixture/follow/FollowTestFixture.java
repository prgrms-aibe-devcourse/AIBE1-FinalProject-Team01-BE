package kr.co.amateurs.server.fixture.follow;

import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

public class FollowTestFixture {

    public static User createUser(String email, String nickname, Role role) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .name(nickname)
                .role(role)
                .build();
        return user;
    }

    public static User createStudentUser(String email, String nickname) {
        return createUser(email, nickname, Role.STUDENT);
    }

    public static User createGuestUser(String email, String nickname) {
        return createUser(email, nickname, Role.GUEST);
    }

    public static User createAnonymousUser(String email, String nickname) {
        return createUser(email, nickname, Role.ANONYMOUS);
    }

    public static User createAdminUser(String email, String nickname) {
        return createUser(email, nickname, Role.ADMIN);
    }


    public static Follow createFollow(User fromUser, User toUser) {
        return Follow.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
    }


    public static Post createPost(String title, String content, User user, BoardType boardType) {
        Post post = Post.builder()
                .user(user)
                .boardType(boardType)
                .title(title)
                .content(content)
                .build();
        return post;
    }


    public static PostStatistics createPostStatistics(Post post, int viewCount) {
        return PostStatistics.builder()
                .post(post)
                .viewCount(viewCount)
                .build();
    }


    public static class DefaultUsers {
        public static final User CURRENT_USER   = createStudentUser("current@test.com",  "currentUser");
        public static final User TARGET_USER_1  = createStudentUser("target1@test.com", "targetUser1");
        public static final User TARGET_USER_2  = createGuestUser(  "target2@test.com", "targetUser2");
        public static final User TARGET_USER_3  = createAnonymousUser("target3@test.com","targetUser3");
        public static final User FOLLOWER_USER_1= createStudentUser("follower1@test.com","follower1");
        public static final User FOLLOWER_USER_2= createGuestUser(  "follower2@test.com","follower2");
    }

    public static class DefaultPosts {
        public static final Post FREE_POST      = createPost("자유 게시글",      "자유 게시글 내용",      DefaultUsers.TARGET_USER_1, BoardType.FREE);
        public static final Post REVIEW_POST    = createPost("리뷰 게시글",      "리뷰 게시글 내용",      DefaultUsers.TARGET_USER_2, BoardType.REVIEW);
        public static final Post PROJECT_POST   = createPost("프로젝트 게시글",  "프로젝트 게시글 내용",  DefaultUsers.TARGET_USER_1, BoardType.PROJECT_HUB);
        public static final Post NEWS_POST      = createPost("뉴스 게시글",      "뉴스 게시글 내용",      DefaultUsers.TARGET_USER_3, BoardType.NEWS);
        public static final Post QNA_POST       = createPost("Q&A 게시글",      "Q&A 게시글 내용",      DefaultUsers.TARGET_USER_2, BoardType.QNA);
        public static final Post RETRO_POST     = createPost("회고 게시글",      "회고 게시글 내용",      DefaultUsers.TARGET_USER_1, BoardType.RETROSPECT);
    }

    public static class DefaultStatistics {
        public static final PostStatistics FREE_STATS    = createPostStatistics(DefaultPosts.FREE_POST,      10);
        public static final PostStatistics REVIEW_STATS  = createPostStatistics(DefaultPosts.REVIEW_POST,    20);
        public static final PostStatistics PROJECT_STATS = createPostStatistics(DefaultPosts.PROJECT_POST,   15);
        public static final PostStatistics NEWS_STATS    = createPostStatistics(DefaultPosts.NEWS_POST,      30);
        public static final PostStatistics QNA_STATS     = createPostStatistics(DefaultPosts.QNA_POST,       25);
        public static final PostStatistics RETRO_STATS   = createPostStatistics(DefaultPosts.RETRO_POST,     18);
    }

    public static class DefaultFollows {
        public static final Follow CURRENT_FOLLOWS_T1 = createFollow(DefaultUsers.CURRENT_USER,   DefaultUsers.TARGET_USER_1);
        public static final Follow CURRENT_FOLLOWS_T2 = createFollow(DefaultUsers.CURRENT_USER,   DefaultUsers.TARGET_USER_2);
        public static final Follow CURRENT_FOLLOWS_T3 = createFollow(DefaultUsers.CURRENT_USER,   DefaultUsers.TARGET_USER_3);
        public static final Follow F1_FOLLOWS_CURRENT = createFollow(DefaultUsers.FOLLOWER_USER_1, DefaultUsers.CURRENT_USER);
        public static final Follow F2_FOLLOWS_CURRENT = createFollow(DefaultUsers.FOLLOWER_USER_2, DefaultUsers.CURRENT_USER);
    }
}
