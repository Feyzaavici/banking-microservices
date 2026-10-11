package org.example.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthenticateRequest(
        @NotBlank @Pattern(regexp = "[0-9]{11}")
        String tcNo,

        @NotBlank
        String password) {

}
