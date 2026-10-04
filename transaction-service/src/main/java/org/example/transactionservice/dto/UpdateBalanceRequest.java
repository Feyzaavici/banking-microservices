package org.example.transactionservice.dto;

import org.example.transactionservice.enums.OperationType;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
        BigDecimal amount,
        OperationType operationTyp
) {
}
