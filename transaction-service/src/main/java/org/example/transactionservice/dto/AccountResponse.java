package org.example.transactionservice.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String iban,
        Long customerId,
        BigDecimal balance,
        String currency,
        String status
) {
}
