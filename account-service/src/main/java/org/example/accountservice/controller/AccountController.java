package org.example.accountservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.dto.UpdateBalanceRequest;
import org.example.accountservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request){
        log.info("Incoming request: create account for customer: {}", request.customerId());

        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<AccountResponse> getAccountByIban(@PathVariable String iban){
        log.info("Incoming request: get account by IBAN: {}", iban);

        AccountResponse response = accountService.getAccountByIban(iban);

        log.info("Account retrieved successfully for IBAN: {}", iban);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable Long customerId) {
        log.info("Incoming request: get account by customerId: {}", customerId);

        List<AccountResponse> response = accountService.getAccountByCustomerId(customerId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{iban}/balance")
    public ResponseEntity<AccountResponse> updateBalance(@PathVariable String iban, @RequestBody UpdateBalanceRequest request){
        log.info("Update balance for customer: {},", iban);

        return ResponseEntity.ok(accountService.updateBalance(iban, request));

    }


}
