package org.example.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CustomerAuthResponse {
    private Long id;
    private String tcNo;
}
