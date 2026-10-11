package org.example.customerservice.controller;

import org.example.customerservice.dto.AuthenticateRequest;
import org.example.customerservice.dto.CreateCustomerRequest;
import org.example.customerservice.dto.CustomerAuthResponse;
import org.example.customerservice.dto.CustomerResponse;
import org.example.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/authenticate")
    public ResponseEntity<CustomerAuthResponse> authenticate(
            @RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(customerService.authenticate(request));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Received request to create customer : {}", request.getTcNo());

        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id,
                                                         @RequestHeader("X-Customer-Id") Long requesterCustomerId) {
        log.info("Received request to get Customer : {}", id);
        CustomerResponse response = customerService.getCustomer(id, requesterCustomerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tc/{tcNo}")
    public ResponseEntity<CustomerResponse> getCustomerByTcNo(@PathVariable String tcNo,
                                                               @RequestHeader("X-Customer-Id") Long requesterCustomerId) {
        log.info("Received request to get Customer by Tc No : {}", tcNo);
        CustomerResponse response = customerService.getCustomer(tcNo, requesterCustomerId);
        return ResponseEntity.ok(response);
    }

}