package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    public List<PostContentData> getWritePosts(Long userId) {
        try {
            List<Post> posts = postRepository.findTop3ByUserIdOrderByCreatedAtDesc(userId);

            List<PostContentData> postContentDataList = posts.stream()
                    .map(post -> new PostContentData(post.getId(), post.getTitle(), post.getContent(), "write"))
                    .toList();
            return postContentDataList;
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }
}
