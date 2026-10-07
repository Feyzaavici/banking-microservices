package org.example.transactionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.example.transactionservice.dto.AccountResponse;
import org.example.transactionservice.dto.UpdateBalanceRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountClientProxy {
    private final AccountClient accountClient;

    @Retry(name = "accountService")
    @CircuitBreaker(name = "accountService")
    public AccountResponse getAccountByIban(String iban){
        return accountClient.getAccountByIban(iban);
    }

    @CircuitBreaker(name = "accountService")
    public AccountResponse updateBalance(String iban, UpdateBalanceRequest request){
        return accountClient.updateBalance(iban,request);
    }
}
