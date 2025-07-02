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

        validateBoardAccess(userRole, targetBoardType, boardAccess);
    }

    private BoardType getBoardType(JoinPoint joinPoint, BoardAccess boardAccess) {
        if (boardAccess.hasPostId()) {
            Post post = findPostById(joinPoint);
            return post.getBoardType();
        }

        throw new CustomException(ErrorCode.NOT_FOUND);
    }

    private void validateBoardAccess(Role userRole, BoardType targetBoardType, BoardAccess boardAccess) {
        boardAccessPolicy.validateAccess(userRole, targetBoardType, boardAccess.operation());
    }


    private Post findPostById(JoinPoint joinPoint) {
        Long postId = extractParameterValue(joinPoint, "postId", Long.class);
        return postRepository.findById(postId)
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