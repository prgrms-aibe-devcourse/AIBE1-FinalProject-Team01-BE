package kr.co.amateurs.server.fixture.comment;

import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

import java.util.ArrayList;

public class CommentTestFixtures {

    public static User createAdminUser() {
        return User.builder()
                .email("admin@test.com")
                .nickname("admin")
                .name("관리자")
                .imageUrl("admin-profile.jpg")
                .role(Role.ADMIN)
                .build();
    }

    public static User createStudentUser() {
        return User.builder()
                .email("student@test.com")
                .nickname("student")
                .name("수강생")
                .imageUrl("student-profile.jpg")
                .role(Role.STUDENT)
                .build();
    }

    public static User createGuestUser() {
        return User.builder()
                .email("guest@test.com")
                .nickname("guest")
                .name("게스트")
                .imageUrl("guest-profile.jpg")
                .role(Role.GUEST)
                .build();
    }

    public static User createAnotherUser() {
        return User.builder()
                .email("another@test.com")
                .nickname("anotherUser")
                .name("다른유저")
                .imageUrl("another-profile.jpg")
                .role(Role.STUDENT)
                .build();
    }

    public static User createCustomUser(String email, String nickname, String name, Role role) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .name(name)
                .imageUrl(nickname + "-profile.jpg")
                .role(role)
                .build();
    }

    public static Post createTestPost(User user) {
        return Post.builder()
                .user(user)
                .title("테스트 게시글")
                .content("테스트 게시글 내용")
                .tags("테스트,댓글")
                .boardType(BoardType.FREE)
                .viewCount(10)
                .likeCount(5)
                .postImages(new ArrayList<>())
                .build();
    }

    public static Post createCustomPost(User user, String title, String content, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags("테스트,태그")
                .boardType(boardType)
                .viewCount(0)
                .likeCount(0)
                .postImages(new ArrayList<>())
                .build();
    }

    public static Comment createRootComment(Post post, User user, String content) {
        return Comment.builder()
                .postId(post.getId())
                .user(user)
                .parentCommentId(null)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    public static Comment createReplyComment(Post post, User user, Comment parentComment, String content) {
        return Comment.builder()
                .postId(post.getId())
                .user(user)
                .parentCommentId(parentComment.getId())
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    public static Comment createComment(Post post, User user, Comment parentComment, String content) {
        Long parentCommentId = null;
        if(parentComment != null){
            parentCommentId = parentComment.getId();
        }

        return Comment.builder()
                .postId(post.getId())
                .user(user)
                .parentCommentId(parentCommentId)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    public static CommentRequestDTO createRootCommentRequestDTO(String content) {
        return new CommentRequestDTO(null, content);
    }

    public static CommentRequestDTO createReplyCommentRequestDTO(Long parentCommentId, String content) {
        return new CommentRequestDTO(parentCommentId, content);
    }

    public static CommentRequestDTO createCommentRequestDTO(Long parentCommentId, String content) {
        return new CommentRequestDTO(parentCommentId, content);
    }

    public static MarketItem createMarketItem(Post post) {
        return MarketItem.builder()
                .post(post)
                .price(10000)
                .place("서울")
                .status(MarketStatus.SELLING)
                .build();
    }
}