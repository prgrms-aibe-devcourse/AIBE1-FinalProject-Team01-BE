package kr.co.amateurs.server.annotation.boardaccess;

import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BoardAccess {
    /**
     * 수행하려는 작업 타입을 지정합니다.
     * READ, WRITE 등의 작업에 따라 다른 권한 정책이 적용됩니다.
     *
     * @return 작업 타입 (기본값: READ)
     */
    OperationType operation() default OperationType.READ;
}