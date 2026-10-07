package org.example.transactionservice.exception;

public class AccountServiceUnavailableException extends BaseException {
    public AccountServiceUnavailableException(Throwable cause) {
        super(MessageType.ACCOUNT_SERVICE_UNAVAILABLE, cause);
    }
}
