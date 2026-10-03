package org.example.accountservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException exception, WebRequest request) {
        HttpStatus status = exception.getMessageType().getStatus();
        ErrorResponse body = createResponse(status,
                exception.getMessage(), request, Map.of());
        return ResponseEntity.status(status).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String name = error instanceof FieldError fieldError
                    ? fieldError.getField() : error.getObjectName();
            String message = error.getDefaultMessage() == null
                    ? "Invalid value" : error.getDefaultMessage();
            errors.computeIfAbsent(name, key -> new ArrayList<>()).add(message);
        });
        ErrorResponse body = createResponse(status, "Validation failed", request, errors);
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Request processing failed", exception);
        }
        ErrorResponse response = body instanceof ErrorResponse errorResponse
                ? errorResponse
                : createResponse(status, status.is5xxServerError()
                        ? "An unexpected error occurred" : reasonPhrase(status), request, Map.of());
        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception, WebRequest request) {
        log.error("Unexpected request processing error", exception);
        ErrorResponse body = createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request, Map.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ErrorResponse createResponse(HttpStatusCode status, String message,
                                         WebRequest request, Map<String, List<String>> errors) {
        String path = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI() : "";
        return new ErrorResponse(LocalDateTime.now(), status.value(), reasonPhrase(status),
                message, path, errors);
    }

    private String reasonPhrase(HttpStatusCode status) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        return httpStatus == null ? "HTTP error" : httpStatus.getReasonPhrase();
    }
}
