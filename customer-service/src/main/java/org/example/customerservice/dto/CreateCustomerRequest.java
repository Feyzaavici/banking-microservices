package org.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(

        @NotBlank
        @Size(min=11, max= 11)
        String tcNo,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String email,

        @NotBlank
        String phoneNumber,

        @NotBlank
        LocalDate birthDate,

        String address


) {
}
