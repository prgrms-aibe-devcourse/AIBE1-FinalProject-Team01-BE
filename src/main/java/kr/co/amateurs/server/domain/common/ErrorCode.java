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
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),

    // dm 관련 에러
    USER_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "해당 채팅방의 참여자가 아닙니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채팅방에 접근할 권한이 없습니다."),
    NOT_FOUND_OTHER_USER(HttpStatus.NOT_FOUND, "DM 대상을 찾을 수 없습니다."),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // 알람 관련 에러
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    ILLEGAL_ALARM_CREATOR(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 알림 생성자 타입입니다."),
    UNSUPPORTED_RESULT_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 결과 타입입니다."),

    // 회원가입 관련 에러
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

    // 회원 정보 수정 관련 에러
    EMPTY_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호를 입력해주세요."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),

    // 로그인 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ANONYMOUS_USER(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 토픽 관련 에러
    TOPICS_REQUIRED(HttpStatus.BAD_REQUEST, "관심 주제를 최소 1개 이상 선택해주세요."),
    TOPICS_TOO_MANY(HttpStatus.BAD_REQUEST, "관심 주제는 최대 3개까지 선택할 수 있습니다."),

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
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다."),

    // REPORT
    REPORT_NOT_FOUND(HttpStatus.BAD_REQUEST, "신고 글을 찾을 수 없습니다."),

    // 댓글
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "자식 댓글에는 댓글을 달 수 없습니다."),
    INVALID_COMMENT_POST_RELATION(HttpStatus.BAD_REQUEST, "게시글안에 해당하는 댓글이 없습니다"),

    // AI 프로필 관련
    ERROR_SUMMARIZE(HttpStatus.INTERNAL_SERVER_ERROR, "활동 요약 생성 중 오류가 발생했습니다."),
    ERROR_AI_PROFILE_GENERATION(HttpStatus.INTERNAL_SERVER_ERROR, "AI 프로필 생성 중 오류가 발생했습니다."),
    ERROR_AI_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 프로필을 찾을 수 없습니다."),
    ERROR_USER_ACTIVITY_INSUFFICIENT(HttpStatus.BAD_REQUEST, "사용자 활동이 부족하여 AI 프로필을 생성할 수 없습니다."),

    // 추천 시스템 관련
    ERROR_AI_RECOMMENDATION_GENERATION(HttpStatus.INTERNAL_SERVER_ERROR, "추천 생성 중 오류가 발생했습니다."),
    ERROR_AI_RECOMMENDATION_SAVE(HttpStatus.INTERNAL_SERVER_ERROR, "추천 게시글 저장 중 오류가 발생했습니다."),
    ERROR_AI_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 추천 게시글을 찾을 수 없습니다."),

    // 임베딩 관련
    ERROR_AI_EMBEDDING_GENERATION(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 생성 중 오류가 발생했습니다."),
    ERROR_AI_EMBEDDING_SEARCH(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 검색 중 오류가 발생했습니다."),
    ERROR_AI_EMBEDDING_INITIALIZE(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 초기화 중 오류가 발생했습니다."), 
    
    // 좋아요 관련
    INVALID_LIKE(HttpStatus.BAD_REQUEST, "좋아요에는 댓글, 게시글 중 하나만 있어야 됩니다."),
    DUPLICATE_LIKE(HttpStatus.CONFLICT, "좋아요가 이미 있습니다."), 
    
    // 북마크 관련
    DUPLICATE_BOOKMARK(HttpStatus.CONFLICT, "북마크가 이미 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public CustomException get() {
        return new CustomException(this);
    }
}
