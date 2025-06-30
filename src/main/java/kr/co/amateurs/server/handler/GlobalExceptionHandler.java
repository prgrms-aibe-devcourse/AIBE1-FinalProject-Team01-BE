package kr.co.amateurs.server.handler;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.amateurs.server.domain.dto.ErrorResponse;
import kr.co.amateurs.server.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(final Exception e, HttpServletRequest request) {
        logError(request, e, "UnhandledException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustom(final CustomException e, HttpServletRequest request) {
        logError(request, e, "CustomException");
        ErrorResponse error = ErrorResponse.from(e, e.getErrorCode().getHttpStatus());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException e, HttpServletRequest request) {
        logError(request, e, "MethodArgumentNotValidException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            final MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        logError(request, e, "MethodArgumentTypeMismatchException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<ErrorResponse> handleMethodValidation(
            final HandlerMethodValidationException e,
            HttpServletRequest request) {

        logError(request, e, "HandlerMethodValidationException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final HttpMessageNotReadableException e,
            HttpServletRequest request) {
        logError(request, e, "HttpMessageNotReadableException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException e,
            HttpServletRequest request) {
        logError(request, e, "IllegalArgumentException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            final AuthorizationDeniedException e,
            HttpServletRequest request) {

        logError(request, e, "AuthorizationDeniedException");
        ErrorResponse error = ErrorResponse.from(e, HttpStatus.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }


    private void logError(HttpServletRequest request, Exception e, String errorMessage) {
        String method = request.getMethod();
        String uri = getFullRequestPath(request);
        String userInfo = getCurrentUser(request);

        log.error("[{}] occurred | {} {} | User: {} | Error: {}",
                errorMessage, method, uri, userInfo, e.getMessage(), e);
    }

    private String getCurrentUser(HttpServletRequest request) {
        String remoteUser = request.getRemoteUser();
        return remoteUser != null ? remoteUser : "anonymous";
    }

    private String getFullRequestPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString != null ? requestURI + "?" + queryString : requestURI;
    }
}
