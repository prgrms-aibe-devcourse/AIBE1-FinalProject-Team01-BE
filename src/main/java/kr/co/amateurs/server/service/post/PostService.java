package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                    .map(post -> new PostContentData(post.getId(), post.getTitle(), post.getContent(), "작성글"))
                    .toList();
            return postContentDataList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }
}
