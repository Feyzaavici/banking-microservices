package org.example.transactionservice.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class AccountClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new AccountClientErrorDecoder();
    }
}
