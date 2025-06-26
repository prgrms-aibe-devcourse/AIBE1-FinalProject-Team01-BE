package kr.co.amateurs.server.fixture.project;

import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;

public class BookmarkFixture {
    public static Bookmark createBookmark(User user, Post post) {
        return Bookmark.builder()
                .user(user)
                .post(post)
                .build();
    }
}
