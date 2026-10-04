package org.example.transactionservice.exception;

public class BaseException extends RuntimeException {
    private final MessageType messageType;

    public BaseException(MessageType messageType) {
        super(messageType.getMessage());
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public BaseException(MessageType messageType, Throwable cause) {
        super(messageType.getMessage(), cause);
        this.messageType = messageType;
    }
}
