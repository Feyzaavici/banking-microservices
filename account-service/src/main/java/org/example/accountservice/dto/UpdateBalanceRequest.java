package org.example.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import org.example.accountservice.enums.OperationType;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
        @NotNull
        BigDecimal amount,
        @NotNull
        OperationType operationTyp
) {
    public BigDecimal getAmount() { return amount; }
    public OperationType getOperationType() { return operationTyp; }
}
