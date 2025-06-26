package kr.co.amateurs.server.fixture.project;

import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;

public class CommentFixture {
    public static Comment createComment(Post post, User user, String content) {
        return Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .build();
    }

    public static Comment createReplyComment(Post post, User user, Comment parentComment, String content) {
        return Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .parentComment(parentComment)
                .build();
    }
}
