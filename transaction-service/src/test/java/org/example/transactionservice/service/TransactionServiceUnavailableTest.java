package org.example.transactionservice.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.example.transactionservice.client.AccountClientProxy;
import org.example.transactionservice.controller.TransactionController;
import org.example.transactionservice.dto.CreateTransactionRequest;
import org.example.transactionservice.entity.Transaction;
import org.example.transactionservice.enums.TransactionStatus;
import org.example.transactionservice.exception.AccountServiceUnavailableException;
import org.example.transactionservice.exception.GlobalExceptionHandler;
import org.example.transactionservice.exception.MessageType;
import org.example.transactionservice.mapper.TransactionMapper;
import org.example.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnavailableTest {

    private static final String SENDER = "TR000000100000000000000001";
    private static final String RECEIVER = "TR000000100000000000000002";

    @Mock private TransactionRepository repository;
    @Mock private AccountClientProxy accountClientProxy;
    @Mock private TransactionMapper mapper;

    private TransactionService service;
    private Transaction transaction;
    private CallNotPermittedException rejection;

    @BeforeEach
    void setUp() {
        service = new TransactionService(repository, accountClientProxy, mapper);
        transaction = new Transaction();
        transaction.setId(1L);

        CircuitBreaker breaker = CircuitBreaker.ofDefaults("accountService");
        breaker.transitionToOpenState();
        rejection = CallNotPermittedException.createCallNotPermittedException(breaker);

        when(mapper.toEntity(any(CreateTransactionRequest.class))).thenReturn(transaction);
        when(repository.save(transaction)).thenReturn(transaction);
        when(accountClientProxy.getAccountByIban(SENDER)).thenThrow(rejection);
    }

    @Test
    void circuitRejectionBecomesServiceUnavailableAndStopsBalanceUpdates() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                SENDER, RECEIVER, new BigDecimal("100.00"), "Test transfer", "TRY"
        );

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOfSatisfying(AccountServiceUnavailableException.class, exception -> {
                    assertThat(exception.getMessageType())
                            .isEqualTo(MessageType.ACCOUNT_SERVICE_UNAVAILABLE);
                    assertThat(exception.getCause()).isSameAs(rejection);
                });

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        verify(repository, times(2)).save(transaction);
        verify(accountClientProxy).getAccountByIban(SENDER);
        verifyNoMoreInteractions(accountClientProxy);
        verify(mapper, never()).toResponse(any(Transaction.class));
    }

    @Test
    void circuitRejectionReturns503ThroughControllerAndExceptionHandler() throws Exception {
        var mvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderIban": "%s",
                                  "receiverIban": "%s",
                                  "amount": 100.00,
                                  "description": "Test transfer",
                                  "currency": "TRY"
                                }
                                """.formatted(SENDER, RECEIVER)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message")
                        .value("Account service is currently unavailable"))
                .andExpect(jsonPath("$.path").value("/api/transactions"));

        verify(accountClientProxy).getAccountByIban(SENDER);
        verifyNoMoreInteractions(accountClientProxy);
    }
}
