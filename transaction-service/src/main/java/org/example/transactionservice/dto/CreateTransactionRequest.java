package org.example.transactionservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank
        @Size(min = 26, max = 26)
        String senderIban,

        @NotBlank
        @Size(min = 26, max = 26)
        String receiverIban,

        @DecimalMin(value = "0.01")
        @NotNull
        BigDecimal amount,


        String description,

        @NotBlank
        String currency




) {
}
