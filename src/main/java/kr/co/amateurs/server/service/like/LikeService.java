package kr.co.amateurs.server.service.like;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.like.Like;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

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
    private final UserService userService;


    public LikeResponseDTO addLikeToPost(Long postId) {
        User currentUser = getCurrentUser();
        validateUser(currentUser.getId());
        Post post = postRepository.findById(postId).orElseThrow();
        Like likeToPost = Like.builder()
                .user(currentUser)
                .post(post)
                .build();
        Like savedLike = likeRepository.save(likeToPost);
        return convertToDTO(savedLike, "post");
    }

    public LikeResponseDTO addLikeToComment(Long commentId) {
        User currentUser = getCurrentUser();
        validateUser(currentUser.getId());
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        Like likeToPost = Like.builder()
                .user(currentUser)
                .comment(comment)
                .build();
        Like savedLike = likeRepository.save(likeToPost);
        return convertToDTO(savedLike, "comment");

    }

    public void removeLikeFromPost(Long postId) {
        User currentUser = getCurrentUser();
        validateUser(currentUser.getId());
        likeRepository.deleteByPostAndUser(postId, currentUser.getId());
    }

    public void removeLikeFromComment(Long commentId) {
        User currentUser = getCurrentUser();
        validateUser(currentUser.getId());
        likeRepository.deleteByCommentAndUser(commentId, currentUser.getId());
    }
    public boolean checkHasLiked(Long postId) {
        User user = getCurrentUser();
        return likeRepository
                .findByPost_IdAndUser_Id(postId, user.getId())
                .isPresent();
    }
    private User getCurrentUser() {
        Optional<User> user = Objects.requireNonNull(userService).getCurrentUser();
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.get();
    }
    private void validateUser(Long userId) {
        User currentUser = getCurrentUser();
        Long currentId = currentUser.getId();
        Role currentRole = currentUser.getRole();
        if (!currentId.equals(userId) && currentRole != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인의 북마크에만 접근할 수 있습니다.");
        }
    }

    public List<PostContentData> getLikedPosts(Long userId) {
        try {
            List<Like> likes = likeRepository.findTop3ByUserIdAndPostIsNotNullOrderByCreatedAtDesc(userId);
            return likes.stream().map(like ->
                    new PostContentData(like.getPost().getId(), like.getPost().getTitle(), like.getPost().getContent(), "좋아요")).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
