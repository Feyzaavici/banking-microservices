package org.example.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;


public record CreateAccountRequest(
        @NotNull
        Long customerId,

        @NotNull
        @Pattern(regexp = "TRY|USD|EUR", message = "Currency must be TRY, USD or EUR")
        String currency,

        @NotNull
        BigDecimal initialBalance
) {
}
