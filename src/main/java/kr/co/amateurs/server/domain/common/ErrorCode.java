package kr.co.amateurs.server.domain.common;

import kr.co.amateurs.server.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements Supplier<CustomException> {
    NOT_FOUND(HttpStatus.NOT_FOUND, "조회할 대상을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.BAD_REQUEST, "접근 할 수 없습니다."),

    // dm
    USER_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "해당 채팅방의 참여자가 아닙니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채팅방에 접근할 권한이 없습니다."),
    NOT_FOUND_OTHER_USER(HttpStatus.NOT_FOUND, "DM 대상을 찾을 수 없습니다."),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // 회원가입 관련 에러 추가
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public CustomException get() {
        return new CustomException(this);
    }
}
