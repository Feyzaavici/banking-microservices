package org.example.customerservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    EMAIl_ALREADY_EXISTS(HttpStatus.CONFLICT,"Email already exists."),
    TC_NO_ALREADY_EXISTS(HttpStatus.CONFLICT, "TC NO already exists"),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND,"Customer not found");


    private final HttpStatus status;
    private final String message;
}
