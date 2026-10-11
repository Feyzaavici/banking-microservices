package org.example.transactionservice.service;

import java.math.BigDecimal;
import org.example.transactionservice.client.AccountClientProxy;
import org.example.transactionservice.dto.AccountResponse;
import org.example.transactionservice.dto.CreateTransactionRequest;
import org.example.transactionservice.entity.Transaction;
import org.example.transactionservice.exception.BaseException;
import org.example.transactionservice.exception.MessageType;
import org.example.transactionservice.mapper.TransactionMapper;
import org.example.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionOwnershipTest {
    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final AccountClientProxy client = mock(AccountClientProxy.class);
    private final TransactionMapper mapper = mock(TransactionMapper.class);
    private final TransactionService service = new TransactionService(repository, client, mapper);

    @Test
    void cannotTransferFromAnotherCustomersAccount() {
        var request = new CreateTransactionRequest("sender", "receiver", BigDecimal.TEN, "test", "TRY");
        var transaction = new Transaction();
        when(mapper.toEntity(request)).thenReturn(transaction);
        when(repository.save(transaction)).thenReturn(transaction);
        when(client.getAccountByIban("sender")).thenReturn(account());
        assertDenied(() -> service.createTransaction(request, 42L));
        verify(client).getAccountByIban("sender");
        verifyNoMoreInteractions(client);
    }

    @Test
    void cannotReadAnotherCustomersTransactionHistory() {
        when(client.getAccountByIban("sender")).thenReturn(account());
        assertDenied(() -> service.getTransactionsByIban("sender", 42L));
        verifyNoInteractions(repository, mapper);
    }

    private AccountResponse account() {
        return new AccountResponse(1L, "sender", 99L, new BigDecimal("100"), "TRY", "ACTIVE");
    }

    private void assertDenied(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOfSatisfying(BaseException.class,
                error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
    }
}
