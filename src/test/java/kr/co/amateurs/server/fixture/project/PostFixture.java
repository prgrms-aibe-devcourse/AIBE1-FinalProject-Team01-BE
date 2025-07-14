package kr.co.amateurs.server.fixture.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;

import java.util.ArrayList;
import java.util.List;

public class PostFixture {
    public static Post createPost(User user, String title, String content, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .boardType(boardType)
                .build();
    }

    public static Post createPostWithTags(User user, String title, String content, BoardType boardType, List<String> tags) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .boardType(boardType)
                .tags(objectMapper.writeValueAsString(tags))
                .postImages(new ArrayList<>())
                .build();
    }
}
