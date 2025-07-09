package kr.co.amateurs.server.annotation.checkpostmetadata;

import kr.co.amateurs.server.domain.common.ErrorCode;
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
public class CheckPostMetaDataAspect {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CheckPostMetaDataPolicy checkPostMetaDataPolicy;

    @Before("@annotation(checkPostMetaData)")
    public void checkBoardAccess(JoinPoint joinPoint, CheckPostMetaData checkPostMetaData) {
        User user = userService.getCurrentUser().orElse(null);
        Role userRole = user != null ? user.getRole() : Role.ANONYMOUS;

        if (userRole == Role.ADMIN) {
            return;
        }

        BoardType targetBoardType = getBoardType(joinPoint, checkPostMetaData);

        validateBoardAccess(userRole, targetBoardType, checkPostMetaData);
    }

    private BoardType getBoardType(JoinPoint joinPoint, CheckPostMetaData checkPostMetaData) {
        return findBoardTypeById(joinPoint);
    }

    private void validateBoardAccess(Role userRole, BoardType targetBoardType, CheckPostMetaData checkPostMetaData) {
        checkPostMetaDataPolicy.validateAccess(userRole, targetBoardType, checkPostMetaData.operation());
    }


    private BoardType findBoardTypeById(JoinPoint joinPoint) {
        Long postId = extractParameterValue(joinPoint, "postId", Long.class);
        return postRepository.findBoardTypeById(postId)
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