package org.example.customerservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    EMAIl_ALREADY_EXISTS(HttpStatus.CONFLICT,"Email already exists."),
    TC_NO_ALREADY_EXISTS(HttpStatus.CONFLICT, "TC NO already exists"),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND,"Customer not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid TC number or password"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "You can only access your own profile");


    private final HttpStatus status;
    private final String message;
}
