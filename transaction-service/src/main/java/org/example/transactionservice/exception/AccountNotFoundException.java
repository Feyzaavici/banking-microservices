package org.example.transactionservice.exception;

public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException(Throwable cause) {
        super(MessageType.ACCOUNT_NOT_FOUND, cause);
    }
}
