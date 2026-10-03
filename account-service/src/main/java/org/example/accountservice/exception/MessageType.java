package org.example.accountservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum MessageType {
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND,"Account not found with IBAN"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.BAD_REQUEST,"Account is not active"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST,"Insufficient balance in account");

    private final HttpStatus status;
    private final String message;



}
