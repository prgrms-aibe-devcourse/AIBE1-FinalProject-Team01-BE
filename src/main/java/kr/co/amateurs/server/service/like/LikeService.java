package kr.co.amateurs.server.service.like;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.like.Like;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static kr.co.amateurs.server.domain.dto.like.LikeResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public LikeResponseDTO addLikeToPost(Long postId, Long userId) {
        User currentUser = userRepository.findById(userId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        Like likeToPost = Like.builder()
                .user(currentUser)
                .post(post)
                .build();
        Like savedLike = likeRepository.save(likeToPost);
        return convertToDTO(savedLike, "post");
    }

    public LikeResponseDTO addLikeToComment(Long commentId, Long userId) {
        User currentUser = userRepository.findById(userId).orElseThrow();
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        Like likeToPost = Like.builder()
                .user(currentUser)
                .comment(comment)
                .build();
        Like savedLike = likeRepository.save(likeToPost);
        return convertToDTO(savedLike, "comment");

    }

    public void removeLikeFromPost(Long postId, Long userId) {
        likeRepository.deleteByPostAndUser(postId, userId);
    }

    public void removeLikeFromComment(Long commentId, Long userId) {
        likeRepository.deleteByCommentAndUser(commentId, userId);
    }

    public List<PostContentData> getLikedPosts(Long userId) {
        try {
            List<Like> likes = likeRepository.findTop3ByUserIdAndPostIsNotNullOrderByCreatedAtDesc(userId);
            return likes.stream().map(like ->
                    new PostContentData(like.getPost().getId(), like.getPost().getTitle(), like.getPost().getContent(), "like")).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
