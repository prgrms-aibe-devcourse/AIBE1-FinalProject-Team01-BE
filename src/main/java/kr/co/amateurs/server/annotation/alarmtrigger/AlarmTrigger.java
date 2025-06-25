package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.annotation.alarmtrigger.extractors.AlarmReceiver;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드에 알람 트리거 기능을 추가하는 어노테이션입니다.
 * 
 * 이 어노테이션이 붙은 메서드가 정상적으로 실행 완료되면
 * 자동으로 지정된 타입의 알람을 생성하여 해당 수신자에게 전송합니다.
 * 
 * 사용 예시:
 * @AlarmTrigger(type = AlarmType.COMMENT, receiver = AlarmReceiver.POST_AUTHOR)
 * public CommentResponse createComment(Long postId, CommentRequest request) {
 *     // 댓글 생성 로직
 *     return response;
 * }
 * 
 * 위의 경우 댓글 생성이 성공하면 게시글 작성자에게 댓글 알람이 자동으로 전송됩니다.
 * 
 * 주의사항:
 * - 메서드가 예외 없이 정상 완료되어야 알람이 발생합니다
 * - 메서드의 반환값은 해당 AlarmProcessor가 처리할 수 있는 타입이어야 합니다
 * - 별도 트랜잭션으로 실행되므로 알람 생성 실패가 메인 로직에 영향을 주지 않습니다
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlarmTrigger {
    
    /**
     * 생성할 알람의 타입을 지정합니다.
     * 
     * 알람 타입에 따라 알람 제목이 결정되며,
     * 클라이언트에서 알람을 분류하고 표시하는 데 사용됩니다.
     * 
     * @return 알람 타입
     */
    AlarmType type();

    /**
     * 알람을 받을 수신자의 타입을 지정합니다.
     * 
     * 이 값에 따라 적절한 AlarmProcessor가 선택되어
     * 실제 수신자 ID 추출 및 메시지 생성이 이루어집니다.
     * 
     * @return 알람 수신자 타입
     */
    AlarmReceiver receiver();
}
