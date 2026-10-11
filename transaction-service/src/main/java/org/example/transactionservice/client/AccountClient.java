package org.example.transactionservice.client;

import org.example.transactionservice.dto.AccountResponse;
import org.example.transactionservice.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", configuration = AccountClientConfig.class)
public interface AccountClient {

    @GetMapping("/internal/accounts/{iban}")
    AccountResponse getAccountByIban(@PathVariable("iban") String iban);

    @PatchMapping("/internal/accounts/{iban}/balance")
    AccountResponse updateBalance(
            @PathVariable("iban") String iban,
            @RequestBody UpdateBalanceRequest request);
}
