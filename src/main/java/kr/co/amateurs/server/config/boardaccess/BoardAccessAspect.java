package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
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

    private final UserService userService;

    @Before("@annotation(boardAccess)")
    public void checkBoardAccess(JoinPoint joinPoint, BoardAccess boardAccess) {
        User user = userService.getCurrentUser().orElse(null);
        Role userRole = user != null ? user.getRole() : Role.ANONYMOUS;

        if (userRole == Role.ADMIN) {
            return;
        }

        BoardType targetBoardType;

        if (boardAccess.hasPostId()) {
            targetBoardType = getBoardTypeFromPostId(joinPoint);
        }
        else if (boardAccess.hasBoardType()) {
            targetBoardType = getBoardTypeFromParameter(joinPoint);
        }
        else {
            targetBoardType = boardAccess.boardType();
        }

        if (boardAccess.needCategory()){
            BoardAccessPolicy.boardTypeInCategory(targetBoardType, boardAccess.category());
        }

        BoardAccessPolicy.validateAccess(userRole, targetBoardType, boardAccess.operation());
    }

    private BoardType getBoardTypeFromPostId(JoinPoint joinPoint) {
        Long postId = getPostIdFromParameter(joinPoint);
        if (postId != null) {
            return postRepository.findById(postId)
                    .map(post -> post.getBoardType())
                    .orElseThrow(ErrorCode.NOT_FOUND);
        }
        throw new CustomException(ErrorCode.NOT_FOUND);
    }

    private BoardType getBoardTypeFromParameter(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

            if (pathVariable != null) {
                String paramName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();

                if ("boardType".equals(paramName) && parameter.getType() == BoardType.class) {
                    return (BoardType) args[i];
                }
            }
        }
        throw new CustomException(ErrorCode.NOT_FOUND);
    }

    private Long getPostIdFromParameter(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

            if (pathVariable != null) {
                String paramName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();

                if ("postId".equals(paramName) && (parameter.getType() == Long.class || parameter.getType() == long.class)) {
                    return (Long) args[i];
                }
            }
        }
        throw new CustomException(ErrorCode.NOT_FOUND);
    }
}