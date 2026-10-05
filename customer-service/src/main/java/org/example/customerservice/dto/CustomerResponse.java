package org.example.customerservice.dto;

import org.example.customerservice.enums.CustomerStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String address,
        String phoneNumber,
        LocalDate birthDate,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
