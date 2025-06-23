package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BoardAccess {
    BoardCategory category() default BoardCategory.COMMUNITY;

    boolean needCategory() default false;

    BoardType boardType() default BoardType.GATHER;

    boolean hasBoardType() default true;

    boolean hasPostId() default false;

    String operation() default "read";
}