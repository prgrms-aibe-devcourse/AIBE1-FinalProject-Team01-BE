package kr.co.amateurs.server.exception;

import kr.co.amateurs.server.domain.common.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public CustomException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}
