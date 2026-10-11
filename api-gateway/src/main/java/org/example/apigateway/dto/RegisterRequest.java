package org.example.apigateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = "[0-9]{11}") String tcNo,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String email,
        @NotBlank String phoneNumber,
        @NotNull LocalDate birthDate,
        String address,
        @NotBlank @Size(min = 6) String password) { }
