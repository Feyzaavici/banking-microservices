package org.example.customerservice.exception;

public class BaseException extends RuntimeException {
    private final MessageType messageType;

    public BaseException(MessageType messageType) {
        super(messageType.getMessage());
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
