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
    ACCESS_DENIED(HttpStatus.BAD_REQUEST, "해당 작업을 수행할 권한이 없습니다."),

    // dm
    USER_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "해당 채팅방의 참여자가 아닙니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채팅방에 접근할 권한이 없습니다."),
    NOT_FOUND_OTHER_USER(HttpStatus.NOT_FOUND, "DM 대상을 찾을 수 없습니다."),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // 회원가입 관련 에러
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

    // 로그인 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 토큰 관련 에러
    EMPTY_EMAIL(HttpStatus.BAD_REQUEST, "이메일은 필수입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "올바른 이메일 형식이 아닙니다."),
    EMPTY_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),
    INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "닉네임은 2자 이상 20자 이하여야 합니다."),
    EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "토큰은 필수입니다."),
    INVALID_EXPIRATION_TIME(HttpStatus.BAD_REQUEST, "만료시간은 양수여야 합니다."),

    // 시스템 에러
    HASH_ALGORITHM_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "해시 알고리즘을 찾을 수 없습니다."),

    // 파일 업로드 관련 에러
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 파일 크기 제한을 초과하였습니다."),

    // POST
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "게시글을 찾을 수 없습니다."),

    // REPORT
    REPORT_NOT_FOUND(HttpStatus.BAD_REQUEST, "신고 글을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public CustomException get() {
        return new CustomException(this);
    }
}
