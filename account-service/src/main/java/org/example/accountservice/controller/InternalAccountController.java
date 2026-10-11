package org.example.accountservice.controller;

import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.UpdateBalanceRequest;
import org.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// service-to-service only (e.g. transaction-service's Feign client) — no ownership checks here
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
@Slf4j
public class InternalAccountController {

    private final AccountService accountService;

    @GetMapping("/{iban}")
    public ResponseEntity<AccountResponse> getAccountByIban(@PathVariable String iban) {
        log.info("Incoming internal request: get account by IBAN: {}", iban);
        AccountResponse response = accountService.getAccountByIbanInternal(iban);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{iban}/balance")
    public ResponseEntity<AccountResponse> updateBalance(@PathVariable String iban,
                                                           @Valid @RequestBody UpdateBalanceRequest request) {
        log.info("Incoming internal request: update balance for IBAN: {}, Operation: {}, Amount: {}",
                iban, request.getOperationType(), request.getAmount());

        AccountResponse response = accountService.updateBalance(iban, request);

        log.info("Balance update request completed for IBAN: {}", iban);
        return ResponseEntity.ok(response);
    }

}
