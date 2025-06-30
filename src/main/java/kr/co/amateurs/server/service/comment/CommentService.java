package kr.co.amateurs.server.service.comment;

import kr.co.amateurs.server.annotation.alarmtrigger.AlarmTrigger;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private static final int CURSOR_OFFSET = 1;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    private final UserService userService;

    public CommentPageDTO getCommentsByPostId(Long postId, Long cursor, int size) {
        Post post = findPostById(postId);

        List<Comment> comments = fetchRootComments(post, cursor, size + CURSOR_OFFSET);
        List<CommentResponseDTO> commentDTOs = convertToRootCommentDTOs(comments);

        return createCommentPageDTO(commentDTOs, size);
    }

    public CommentPageDTO getReplies(Long parentCommentId, Long cursor, int size) {
        Comment parentComment = findCommentById(parentCommentId);

        List<Comment> comments = fetchReplies(parentComment, cursor, size + CURSOR_OFFSET);
        List<CommentResponseDTO> commentDTOs = convertToReplyDTOs(comments);

        return createCommentPageDTO(commentDTOs, size);
    }

    @Transactional
    @AlarmTrigger(type = AlarmType.COMMENT)
    public CommentResponseDTO createComment(Long postId, CommentRequestDTO requestDTO) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        Post post = findPostById(postId);

        Comment parentComment = getParentComment(requestDTO.parentCommentId()).orElse(null);

        Comment comment = Comment.from(requestDTO, post, user, parentComment);
        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDTO.from(savedComment, 0, false);
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequestDTO requestDTO) {
        Comment comment = findCommentById(commentId);

        validateCommentAccess(comment.getUser().getId());

        comment.updateContent(requestDTO.content());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = findCommentById(commentId);

        validateCommentAccess(comment.getUser().getId());

        commentRepository.delete(comment);
    }

    private void validateCommentAccess(Long commentUserId) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        if (!commentUserId.equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private CommentPageDTO createCommentPageDTO(List<CommentResponseDTO> commentDTOs, int size) {
        boolean hasMore = commentDTOs.size() > size;
        if (hasMore) {
            commentDTOs = commentDTOs.subList(0, size);
        }

        Long nextCursor = hasMore ? commentDTOs.get(commentDTOs.size() - 1).id() : null;

        return new CommentPageDTO(commentDTOs, nextCursor, hasMore);
    }

    private Optional<Comment> getParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return Optional.empty();
        }

        Comment parentComment = findCommentById(parentCommentId);
        if (parentComment.getParentComment() != null) {
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        return Optional.of(parentComment);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }

    private List<Comment> fetchRootComments(Post post, Long cursor, int size) {
        if (cursor == null) {
            return commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post, PageRequest.of(0, size));
        } else{
            return commentRepository.findByPostAndParentCommentIsNullAndIdGreaterThanOrderByCreatedAtAsc(post, cursor, PageRequest.of(0, size));
        }
    }

    private List<Comment> fetchReplies(Comment parentComment, Long cursor, int size) {
        if (cursor == null) {
            return commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment, PageRequest.of(0, size));
        } else {
            return commentRepository.findByParentCommentAndIdGreaterThanOrderByCreatedAtAsc(parentComment, cursor, PageRequest.of(0, size));
        }
    }

    private List<CommentResponseDTO> convertToRootCommentDTOs(List<Comment> comments) {
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> replyCountMap = getReplyCountMap(comments);
        Map<Long, Boolean> likeStatusMap = getUserLikeStatusMap(comments);


        return comments.stream()
                .map(comment -> CommentResponseDTO.from(
                        comment,
                        replyCountMap.getOrDefault(comment.getId(), 0),
                        likeStatusMap.getOrDefault(comment.getId(), false)
                ))
                .toList();
    }

    private List<CommentResponseDTO> convertToReplyDTOs(List<Comment> comments) {
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Boolean> likeStatusMap = getUserLikeStatusMap(comments);

        return comments.stream()
                .map(comment -> CommentResponseDTO.from(
                        comment,
                        0,
                        likeStatusMap.getOrDefault(comment.getId(), false)
                ))
                .toList();
    }

    private Map<Long, Integer> getReplyCountMap(List<Comment> comments) {
        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        return commentRepository.countRepliesByParentIds(commentIds).stream()
                .filter(replyCount -> replyCount.getParentCommentId()!=null && replyCount.getCount() != null)
                .collect(Collectors.toMap(
                        replyCount -> replyCount.getParentCommentId(),
                        replyCount -> replyCount.getCount().intValue()
                ));
    }

    private Map<Long, Boolean> getUserLikeStatusMap(List<Comment> comments) {
        User user = userService.getCurrentUser().orElse(null);

        if (user == null) {
            return comments.stream()
                    .collect(Collectors.toMap(Comment::getId, comment -> false));
        }

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        Set<Long> likedCommentIds = likeRepository.findByCommentIdsAndUserId(commentIds, user.getId());

        return comments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        comment -> likedCommentIds.contains(comment.getId())
                ));
    }
}