package org.example.accountservice.dto;

import org.example.accountservice.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String iban,
        Long customerId,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {
    public String getIban() { return iban; }
}
