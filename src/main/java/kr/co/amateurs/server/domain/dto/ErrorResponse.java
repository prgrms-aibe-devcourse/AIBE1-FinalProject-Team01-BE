package kr.co.amateurs.server.domain.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

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
}
