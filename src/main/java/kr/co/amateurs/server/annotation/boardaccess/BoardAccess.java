package kr.co.amateurs.server.annotation.boardaccess;

import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BoardAccess {
    /**
     * 검증할 게시판 카테고리를 지정합니다.
     * needCategory가 true일 때만 사용됩니다.
     * 현재는 COMMUNITY 카테고리만 boardType 비교에 사용하고 있습니다.
     * 
     * @return 게시판 카테고리 (기본값: COMMUNITY)
     */
    BoardCategory category() default BoardCategory.COMMUNITY;

    /**
     * 카테고리 검증이 필요한지 여부를 설정합니다.
     * true로 설정하면 boardType이 지정된 category에 속하는지 검증합니다.
     *
     * @return 카테고리 검증 필요 여부 (기본값: false)
     */
    boolean needCategory() default false;

    /**
     * 검증할 게시판 타입을 지정합니다.
     * hasBoardType이 false이고 hasPostId도 false일 때 이 값이 사용됩니다.
     *
     * @return 게시판 타입 (기본값: GATHER)
     */
    BoardType boardType() default BoardType.GATHER;

    /**
     * 메소드 파라미터에서 BoardType을 추출할지 여부를 설정합니다.
     * true로 설정하면 @PathVariable("boardType") BoardType 파라미터에서 값을 가져옵니다.
     *
     * @return BoardType 파라미터 사용 여부 (기본값: true)
     */
    boolean hasBoardType() default true;

    /**
     * 메소드 파라미터에서 postId를 추출하여 해당 게시글의 BoardType을 조회할지 여부를 설정합니다.
     * true로 설정하면 @PathVariable("postId") Long 파라미터에서 postId를 가져와
     * 데이터베이스에서 해당 게시글의 BoardType을 조회합니다.
     *
     * @return postId 파라미터 사용 여부 (기본값: false)
     */
    boolean hasPostId() default false;

    /**
     * 수행하려는 작업 타입을 지정합니다.
     * READ, WRITE 등의 작업에 따라 다른 권한 정책이 적용됩니다.
     *
     * @return 작업 타입 (기본값: READ)
     */
    Operation operation() default Operation.READ;
}