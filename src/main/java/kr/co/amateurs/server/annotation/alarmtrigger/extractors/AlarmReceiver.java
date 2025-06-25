package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

/**
 * 알람을 받을 수신자의 타입을 정의하는 열거형입니다.
 * 
 * 각 알람 상황별로 누가 알람을 받아야 하는지를 명시합니다.
 * 새로운 알람 타입이 추가될 때마다 해당하는 수신자 타입을 여기에 추가해야 합니다.
 */
public enum AlarmReceiver {
    
    /**
     * 다이렉트 메시지의 수신자
     * DM을 받은 사용자에게 알람을 보낼 때 사용됩니다.
     */
    DIRECT_MESSAGE_RECEIVER, 
    
    /**
     * 게시글의 작성자
     * 본인 게시글에 댓글이 달렸을 때 게시글 작성자에게 알람을 보낼 때 사용됩니다.
     */
    POST_AUTHOR
}
