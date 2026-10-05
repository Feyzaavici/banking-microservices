package org.example.customerservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CreateCustomerRequest;
import org.example.customerservice.dto.CustomerResponse;
import org.example.customerservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request){
        log.info("Received request to create customer: {}",request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));

    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("Received request to get Customer : {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/tc/{tcNo}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String tcNo) {
        log.info("Received request to get Customer by Tc No : {}", tcNo);
        CustomerResponse response = customerService.getCustomer(tcNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("Received request to get all Customers");
        List<CustomerResponse> response = customerService.getAllCustomers();
        return ResponseEntity.ok(response);

    }

}






