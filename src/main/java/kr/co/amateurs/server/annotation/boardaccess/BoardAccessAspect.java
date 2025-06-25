package kr.co.amateurs.server.annotation.boardaccess;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Parameter;


import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BoardAccessAspect {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BoardAccessPolicy boardAccessPolicy;

    @Before("@annotation(boardAccess)")
    public void checkBoardAccess(JoinPoint joinPoint, BoardAccess boardAccess) {
        User user = userService.getCurrentUser().orElse(null);
        Role userRole = user != null ? user.getRole() : Role.ANONYMOUS;

        if (userRole == Role.ADMIN) {
            return;
        }

        BoardType targetBoardType = getBoardType(joinPoint, boardAccess);

        validateCategoryAccess(boardAccess, targetBoardType);
        validateBoardAccess(userRole, targetBoardType, boardAccess);
        validateAuthorAccess(joinPoint, boardAccess, user);
    }

    private BoardType getBoardType(JoinPoint joinPoint, BoardAccess boardAccess) {
        if (boardAccess.hasPostId()) {
            Post post = findPostById(joinPoint);
            return post.getBoardType();
        }

        if (boardAccess.hasBoardType()) {
            return extractParameterValue(joinPoint, "boardType", BoardType.class);
        }

        return boardAccess.boardType();
    }

    private void validateCategoryAccess(BoardAccess boardAccess, BoardType targetBoardType) {
        if (boardAccess.needCategory()) {
            boardAccessPolicy.boardTypeInCategory(targetBoardType, boardAccess.category());
        }
    }

    private void validateBoardAccess(Role userRole, BoardType targetBoardType, BoardAccess boardAccess) {
        boardAccessPolicy.validateAccess(userRole, targetBoardType, boardAccess.operation());
    }

    private void validateAuthorAccess(JoinPoint joinPoint, BoardAccess boardAccess, User user) {
        if (!boardAccess.checkAuthor()) {
            return;
        }

        if (user == null) {
            throw ErrorCode.USER_NOT_FOUND.get();
        }

        if (boardAccess.isComment()) {
            validateCommentAuthor(joinPoint, user);
        } else {
            validatePostAuthor(joinPoint, user);
        }
    }

    private void validateCommentAuthor(JoinPoint joinPoint, User user) {
        Comment comment = findCommentById(joinPoint);
        if (!comment.getUser().getId().equals(user.getId())) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    private void validatePostAuthor(JoinPoint joinPoint, User user) {
        Post post = findPostById(joinPoint);
        if (!post.getUser().getId().equals(user.getId())) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    private Post findPostById(JoinPoint joinPoint) {
        Long postId = extractParameterValue(joinPoint, "postId", Long.class);
        return postRepository.findById(postId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }

    private Comment findCommentById(JoinPoint joinPoint) {
        Long commentId = extractParameterValue(joinPoint, "commentId", Long.class);
        return commentRepository.findById(commentId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }

    private <T> T extractParameterValue(JoinPoint joinPoint, String parameterName, Class<T> parameterType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

            if (pathVariable != null) {
                String actualParamName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();

                if (parameterName.equals(actualParamName) && parameter.getType() == parameterType) {
                    return (T) args[i];
                }
            }
        }
        throw new CustomException(ErrorCode.NOT_FOUND);
    }
}