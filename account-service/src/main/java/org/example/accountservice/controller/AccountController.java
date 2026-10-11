package org.example.accountservice.controller;

import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
                                                         @RequestHeader("X-Customer-Id") Long customerId) {
        log.info("Incoming request: create account for customer: {}", request.getCustomerId());

        AccountResponse response = accountService.createAccount(request, customerId);

        log.info("Account creation request completed for IBAN: {}", response.getIban());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{iban}")
    public ResponseEntity<AccountResponse> getAccountByIban(@PathVariable String iban,
                                                            @RequestHeader("X-Customer-Id") Long customerId) {
        log.info("Incoming request: get account by IBAN: {}", iban);

        AccountResponse response = accountService.getAccountByIban(iban, customerId);

        log.info("Account retrieved successfully for IBAN: {}", iban);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable Long customerId,
                                                                         @RequestHeader("X-Customer-Id") Long requesterCustomerId) {
        log.info("Incoming request: get accounts by customerId: {}", customerId);

        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId, requesterCustomerId);

        log.info("Found {} account(s) for customerId: {}", response.size(), customerId);
        return ResponseEntity.ok(response);
    }

}
