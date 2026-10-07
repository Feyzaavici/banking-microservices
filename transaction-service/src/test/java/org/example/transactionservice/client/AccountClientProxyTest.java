package org.example.transactionservice.client;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.springboot.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import org.example.transactionservice.dto.AccountResponse;
import org.example.transactionservice.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AccountClientProxyTest {
    private static final String IBAN = "test-iban";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CircuitBreakerAutoConfiguration.class,
                    RetryAutoConfiguration.class
            ))
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "resilience4j.circuitbreaker.instances.accountService.sliding-window-type=COUNT_BASED",
                    "resilience4j.circuitbreaker.instances.accountService.sliding-window-size=10",
                    "resilience4j.circuitbreaker.instances.accountService.minimum-number-of-calls=5",
                    "resilience4j.circuitbreaker.instances.accountService.failure-rate-threshold=50",
                    "resilience4j.circuitbreaker.instances.accountService.wait-duration-in-open-state=10s",
                    "resilience4j.circuitbreaker.instances.accountService.permitted-number-of-calls-in-half-open-state=3",
                    "resilience4j.circuitbreaker.instances.accountService.ignore-exceptions[0]="
                            + AccountNotFoundException.class.getName(),
                    "resilience4j.retry.instances.accountService.max-attempts=3",
                    "resilience4j.retry.instances.accountService.wait-duration=1ms",
                    "resilience4j.retry.instances.accountService.retry-exceptions[0]=feign.RetryableException"
            );

    @Test
    void opensCircuitAfterFailuresAndBlocksNextCall() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            AccountClient client = context.getBean(AccountClient.class);
            AccountClientProxy proxy =
                    context.getBean(AccountClientProxy.class);

            CircuitBreaker circuitBreaker = context
                    .getBean(CircuitBreakerRegistry.class)
                    .circuitBreaker("accountService");

            when(client.getAccountByIban(IBAN))
                    .thenThrow(new IllegalStateException(
                            "Simulated service failure"
                    ));

            for (int i = 0; i < 5; i++) {
                assertThatThrownBy(() -> proxy.getAccountByIban(IBAN))
                        .isInstanceOf(IllegalStateException.class);
            }

            assertThat(circuitBreaker.getState())
                    .isEqualTo(CircuitBreaker.State.OPEN);

            assertThatThrownBy(() -> proxy.getAccountByIban(IBAN))
                    .isInstanceOf(CallNotPermittedException.class);

            verify(client, times(5)).getAccountByIban(IBAN);
        });
    }

    @Test
    void accountNotFoundDoesNotOpenCircuit() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            AccountClient client = context.getBean(AccountClient.class);
            AccountClientProxy proxy =
                    context.getBean(AccountClientProxy.class);

            CircuitBreaker circuitBreaker = context
                    .getBean(CircuitBreakerRegistry.class)
                    .circuitBreaker("accountService");

            when(client.getAccountByIban(IBAN))
                    .thenThrow(new AccountNotFoundException(
                            new RuntimeException("Simulated 404")
                    ));

            for (int i = 0; i < 10; i++) {
                assertThatThrownBy(() -> proxy.getAccountByIban(IBAN))
                        .isInstanceOf(AccountNotFoundException.class);
            }

            assertThat(circuitBreaker.getState())
                    .isEqualTo(CircuitBreaker.State.CLOSED);

            assertThat(circuitBreaker.getMetrics()
                    .getNumberOfFailedCalls()).isZero();

            verify(client, times(10)).getAccountByIban(IBAN);
        });
    }

    @Test
    void retriesTransientFailureAndReturnsSuccessfulResponse() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            AccountClient client = context.getBean(AccountClient.class);
            AccountClientProxy proxy = context.getBean(AccountClientProxy.class);
            AccountResponse expected = new AccountResponse(
                    1L, IBAN, 42L, new BigDecimal("1000.00"), "TRY", "ACTIVE"
            );
            RetryableException temporaryFailure = mock(RetryableException.class);

            when(client.getAccountByIban(IBAN))
                    .thenThrow(temporaryFailure)
                    .thenReturn(expected);

            assertThat(proxy.getAccountByIban(IBAN)).isEqualTo(expected);
            verify(client, times(2)).getAccountByIban(IBAN);
        });
    }

    @Test
    void stopsAfterThreeAttemptsWhenTransientFailureContinues() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            AccountClient client = context.getBean(AccountClient.class);
            AccountClientProxy proxy = context.getBean(AccountClientProxy.class);
            RetryableException temporaryFailure = mock(RetryableException.class);

            when(client.getAccountByIban(IBAN)).thenThrow(temporaryFailure);

            assertThatThrownBy(() -> proxy.getAccountByIban(IBAN))
                    .isSameAs(temporaryFailure);
            verify(client, times(3)).getAccountByIban(IBAN);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        AccountClient accountClient() {
            return mock(AccountClient.class);
        }

        @Bean
        AccountClientProxy accountClientProxy(AccountClient accountClient) {
            return new AccountClientProxy(accountClient);
        }
    }
}
