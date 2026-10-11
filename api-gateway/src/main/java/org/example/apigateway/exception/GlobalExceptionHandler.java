package org.example.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ErrorResponse> handleDownstream(
            DownstreamException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatus().value());
        return buildResponse(status, ex.getMessage(), exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, ServerWebExchange exchange) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), exchange);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );
        return ResponseEntity.status(status).body(body);
    }

}
