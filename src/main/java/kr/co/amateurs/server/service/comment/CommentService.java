package kr.co.amateurs.server.service.comment;

import kr.co.amateurs.server.annotation.alarmtrigger.AlarmTrigger;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentJooqDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentJooqRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private static final int CURSOR_OFFSET = 1;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentJooqRepository commentJooqRepository;
    private final UserService userService;

    public CommentPageDTO getCommentsByPostId(Long postId, Long cursor, int size) {
        Optional<User> currentUser = userService.getCurrentUser();
        PageRequest pageRequest = PageRequest.of(0, size + CURSOR_OFFSET);

        boolean isBlinded = postRepository.findIsBlindedByPostId(postId);
        if (isBlinded) {
            return new CommentPageDTO(Collections.emptyList(), null, false);
        }

        List<CommentJooqDTO> comments = fetchRootComments(
                postId,
                currentUser.map(User::getId).orElse(null),
                cursor,
                pageRequest
        );

        List<CommentResponseDTO> responseComments = comments.stream()
                .map(CommentJooqDTO::toResponseDTO)
                .map(CommentResponseDTO::applyBlindFilter)
                .collect(Collectors.toList());

        return createCommentPageDTO(responseComments, size);
    }

    public CommentPageDTO getReplies(Long postId, Long parentCommentId, Long cursor, int size) {
        Comment parentComment = commentRepository.findByIdAndPostId(parentCommentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_COMMENT_POST_RELATION));

        Optional<User> currentUser = userService.getCurrentUser();

        PageRequest pageRequest = PageRequest.of(0, size + CURSOR_OFFSET);

        List<CommentJooqDTO> replies = fetchReplies(
                parentCommentId,
                currentUser.map(User::getId).orElse(null),
                cursor,
                pageRequest
        );

        List<CommentResponseDTO> responseReplies = replies.stream()
                .map(CommentJooqDTO::toResponseDTO)
                .collect(Collectors.toList());

        return createCommentPageDTO(responseReplies, size);
    }

    @Transactional
    @AlarmTrigger(type = AlarmType.COMMENT)
    public CommentResponseDTO createComment(Long postId, CommentRequestDTO requestDTO) {
        User user = userService.getCurrentLoginUser();
        if (!postRepository.existsByIdUsingCount(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        Comment comment = Comment.from(requestDTO, postId, user, requestDTO.parentCommentId());

        if (requestDTO.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(requestDTO.parentCommentId()).orElseThrow(ErrorCode.NOT_FOUND);
            if (parentComment.getParentCommentId() != null){
                throw ErrorCode.INVALID_PARENT_COMMENT.get();
            }
            parentComment.incrementReplyCount();
        }

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDTO.from(savedComment, 0, false);
    }

    @Transactional
    public void updateComment(Long postId, Long commentId, CommentRequestDTO requestDTO) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateCommentAccess(comment.getUser().getId());

        comment.updateContent(requestDTO.content());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateCommentAccess(comment.getUser().getId());

        if (comment.getParentCommentId() != null) {
            commentRepository.decreaseReplyCount(comment.getParentCommentId());
        }

        commentRepository.delete(comment);
    }

    private List<CommentJooqDTO> fetchRootComments(Long postId, Long userId, Long cursor, PageRequest pageRequest) {
        if (userId != null) {
            return (cursor == null)
                    ? commentJooqRepository.findRootCommentsWithLikes(postId, userId, pageRequest)
                    : commentJooqRepository.findRootCommentsAfterCursorWithLikes(postId, cursor, userId, pageRequest);
        } else {
            return (cursor == null)
                    ? commentJooqRepository.findRootCommentsForGuest(postId, pageRequest)
                    : commentJooqRepository.findRootCommentsAfterCursorForGuest(postId, cursor, pageRequest);
        }
    }

    private List<CommentJooqDTO> fetchReplies(Long parentCommentId, Long userId, Long cursor, PageRequest pageRequest) {
        if (userId != null) {
            return (cursor == null)
                    ? commentJooqRepository.findRepliesWithLikes(parentCommentId, userId, pageRequest)
                    : commentJooqRepository.findRepliesAfterCursorWithLikes(parentCommentId, cursor, userId, pageRequest);
        } else {
            return (cursor == null)
                    ? commentJooqRepository.findRepliesForGuest(parentCommentId, pageRequest)
                    : commentJooqRepository.findRepliesAfterCursorForGuest(parentCommentId, cursor, pageRequest);
        }
    }

    private void validateCommentAccess(Long commentUserId) {
        User user = userService.getCurrentLoginUser();

        if (!(commentUserId.equals(user.getId()) || user.getRole() == Role.ADMIN)) {
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

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }
}