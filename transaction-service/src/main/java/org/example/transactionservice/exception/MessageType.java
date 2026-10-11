package org.example.transactionservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    SAME_ACCOUNT(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "You can only access your own accounts"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Amount must be positive"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Account is not active"),
    CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "Account currencies must match the transfer currency"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient balance"),
    ACCOUNT_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Account service request failed"),
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction failed"),
    ACCOUNT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Account service is currently unavailable");

    private final HttpStatus status;
    private final String message;
}
