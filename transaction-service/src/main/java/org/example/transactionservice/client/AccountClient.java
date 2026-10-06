package org.example.transactionservice.client;

import org.example.transactionservice.dto.AccountResponse;
import org.example.transactionservice.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/api/accounts/iban/{iban}")
    AccountResponse getAccountByIban(@PathVariable("iban") String iban);

    @PostMapping("/api/accounts/{iban}/balance")
    AccountResponse updateBalance(
            @PathVariable("iban") String iban,
            @RequestBody UpdateBalanceRequest request);
}
