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
     * 메소드 파라미터에서 postId를 추출하여 해당 게시글의 BoardType을 조회할지 여부를 설정합니다.
     * true로 설정하면 @PathVariable("postId") Long 파라미터에서 postId를 가져와
     * 데이터베이스에서 해당 게시글의 BoardType을 조회합니다.
     *
     * @return postId 파라미터 사용 여부 (기본값: false)
     */
    boolean hasPostId() default true;

    /**
     * 수행하려는 작업 타입을 지정합니다.
     * READ, WRITE 등의 작업에 따라 다른 권한 정책이 적용됩니다.
     *
     * @return 작업 타입 (기본값: READ)
     */
    OperationType operation() default OperationType.READ;
}