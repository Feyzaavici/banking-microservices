package org.example.transactionservice.dto;

import org.example.transactionservice.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,
        String senderIban,
        String receiverIban,
        BigDecimal amount,
        TransactionStatus status,
        String currency,
        String description,
        LocalDateTime createdAt
) {
}
