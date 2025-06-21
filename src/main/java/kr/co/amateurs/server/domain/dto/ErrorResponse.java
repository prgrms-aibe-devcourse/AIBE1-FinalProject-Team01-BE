package kr.co.amateurs.server.domain.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

public record ErrorResponse(String message, String statusMessage, Integer statusCode) {
    @Builder
    public ErrorResponse(String message, String statusMessage, Integer statusCode) {
        this.message = message;
        this.statusMessage = statusMessage;
        this.statusCode = statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public static ErrorResponse from(Exception exception, HttpStatus status) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .statusMessage(status.getReasonPhrase())
                .statusCode(status.value())
                .build();
    }

    public static ErrorResponse from(MethodArgumentNotValidException exception, HttpStatus status) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("유효하지 않은 요청입니다.");

        return ErrorResponse.builder()
                .message(message)
                .statusMessage(status.getReasonPhrase())
                .statusCode(status.value())
                .build();
    }
}
