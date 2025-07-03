package kr.co.amateurs.server.service.like;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
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
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public LikeResponseDTO addLikeToPost(Long postId) {
        User currentUser = userService.getCurrentLoginUser();
        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.NOT_FOUND);
        if(checkHasLiked(postId, currentUser.getId())) {
            throw ErrorCode.DUPLICATE_LIKE.get();
        }

        Like likeToPost = Like.builder()
                .user(currentUser)
                .post(post)
                .build();
        Like savedLike = likeRepository.save(likeToPost);

        post.incrementLikeCount();

        return convertToDTO(savedLike, "post");
    }

    @Transactional
    public LikeResponseDTO addLikeToComment(Long postId, Long commentId) {
        User currentUser = userService.getCurrentLoginUser();
        Comment comment = commentRepository.findById(commentId).orElseThrow(ErrorCode.NOT_FOUND);

        validateCommentBelongsToPost(comment, postId);
        if (checkCommentHasLiked(commentId, currentUser.getId())){
            throw ErrorCode.DUPLICATE_LIKE.get();
        }

        Like likeToPost = Like.builder()
                .user(currentUser)
                .comment(comment)
                .build();
        Like savedLike = likeRepository.save(likeToPost);

        comment.incrementLikeCount();

        return convertToDTO(savedLike, "comment");

    }

    @Transactional
    public void removeLikeFromPost(Long postId) {
        User currentUser = userService.getCurrentLoginUser();
        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.NOT_FOUND);

        if(!checkHasLiked(postId, currentUser.getId())) {
            throw ErrorCode.NOT_FOUND.get();
        }

        likeRepository.deleteByPostIdAndUserId(postId, currentUser.getId());

        post.decrementLikeCount();
    }

    @Transactional
    public void removeLikeFromComment(Long postId, Long commentId) {
        User currentUser = userService.getCurrentLoginUser();
        Comment comment = commentRepository.findById(commentId).orElseThrow(ErrorCode.NOT_FOUND);

        validateCommentBelongsToPost(comment, postId);
        if(!checkCommentHasLiked(commentId, currentUser.getId())) {
            throw ErrorCode.NOT_FOUND.get();
        }

        likeRepository.deleteByCommentIdAndUserId(commentId, currentUser.getId());

        comment.decrementLikeCount();
    }

    public boolean checkHasLiked(Long postId, Long userId) {
        return likeRepository
                .existsByPost_IdAndUser_Id(postId, userId);
    }

    public boolean checkCommentHasLiked(Long commentId, Long userId) {
        return likeRepository
                .existsByComment_IdAndUser_Id(commentId, userId);
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

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.INVALID_COMMENT_POST_RELATION);
        }
    }
}
