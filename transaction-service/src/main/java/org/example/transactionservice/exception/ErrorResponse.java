package org.example.transactionservice.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, List<String>> validationErrors
) {
}
