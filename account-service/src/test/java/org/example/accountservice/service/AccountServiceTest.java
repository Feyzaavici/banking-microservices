package org.example.accountservice.service;

import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.dto.UpdateBalanceRequest;
import org.example.accountservice.entity.Account;
import org.example.accountservice.enums.AccountStatus;
import org.example.accountservice.enums.OperationType;
import org.example.accountservice.exception.BaseException;
import org.example.accountservice.exception.MessageType;
import org.example.accountservice.mapper.AccountMapper;
import org.example.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final String IBAN = "TR000000100000000000000001";

    @Mock
    private AccountRepository repository;
    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(repository, Mappers.getMapper(AccountMapper.class));
    }

    @Test
    void createAccountPersistsGeneratedIbanAndInitialBalance() {
        when(repository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            assertThat(account.getIban()).matches("TR[0-9]{24}");
            assertThat(account.getCustomerId()).isEqualTo(42L);
            assertThat(account.getCurrency()).isEqualTo("TRY");
            assertThat(account.getBalance()).isEqualByComparingTo("125.50");
            account.setId(1L);
            return account;
        });

        var response = service.createAccount(new CreateAccountRequest(42L, "TRY", new BigDecimal("125.50")));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.iban()).matches("TR[0-9]{24}");
        assertThat(response.balance()).isEqualByComparingTo("125.50");
        verify(repository).save(any(Account.class));
    }

    @Test
    void getAccountByIbanReturnsAccountIncludingTimestamps() {
        Account account = account();
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        var response = service.getAccountByIbanInternal(IBAN);

        assertThat(response.id()).isEqualTo(account.getId());
        assertThat(response.iban()).isEqualTo(IBAN);
        assertThat(response.customerId()).isEqualTo(42L);
        assertThat(response.balance()).isEqualByComparingTo("100.00");
        assertThat(response.currency()).isEqualTo("TRY");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(account.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(account.getUpdatedAt());
    }

    @Test
    void getMissingAccountThrowsAccountNotFound() {
        when(repository.findByIban(IBAN)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getAccountByIbanInternal(IBAN))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCOUNT_NOT_FOUND));
    }

    @Test
    void getAccountsByCustomerReturnsMappedList() {
        Account first = account();
        Account second = account();
        second.setId(2L);
        when(repository.findByCustomerId(42L)).thenReturn(List.of(first, second));
        assertThat(service.getAccountsByCustomerId(42L)).extracting("id").containsExactly(1L, 2L);
    }

    @Test
    void getAccountsByCustomerReturnsEmptyListWhenNoneExist() {
        when(repository.findByCustomerId(42L)).thenReturn(List.of());
        assertThat(service.getAccountsByCustomerId(42L)).isEmpty();
    }

    @Test
    void depositIncreasesBalanceWithoutLosingDecimalPrecision() {
        Account account = account();
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        var response = service.updateBalance(IBAN,
                new UpdateBalanceRequest(new BigDecimal("25.35"), OperationType.DEPOSIT));
        assertThat(response.balance()).isEqualByComparingTo("125.35");
        verify(repository).save(account);
    }

    @Test
    void withdrawDecreasesBalance() {
        Account account = account();
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        var response = service.updateBalance(IBAN,
                new UpdateBalanceRequest(new BigDecimal("25.35"), OperationType.WITHDRAW));
        assertThat(response.balance()).isEqualByComparingTo("74.65");
        verify(repository).save(account);
    }

    @Test
    void withdrawingExactBalanceLeavesZero() {
        Account account = account();
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        var response = service.updateBalance(IBAN,
                new UpdateBalanceRequest(new BigDecimal("100.00"), OperationType.WITHDRAW));
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void insufficientBalanceDoesNotChangeOrSaveAccount() {
        Account account = account();
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.updateBalance(IBAN,
                new UpdateBalanceRequest(new BigDecimal("100.01"), OperationType.WITHDRAW)))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.INSUFFICIENT_BALANCE));
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void inactiveAccountsCannotChangeBalance(AccountStatus status) {
        Account account = account();
        account.setStatus(status);
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.updateBalance(IBAN,
                new UpdateBalanceRequest(BigDecimal.ONE, OperationType.DEPOSIT)))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCOUNT_NOT_ACTIVE));
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        verify(repository, never()).save(any());
    }

    @Test
    void updatingMissingAccountThrowsAndDoesNotSave() {
        when(repository.findByIban(IBAN)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateBalance(IBAN,
                new UpdateBalanceRequest(BigDecimal.ONE, OperationType.DEPOSIT)))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCOUNT_NOT_FOUND));
        verify(repository, never()).save(any());
    }

    private Account account() {
        return Account.builder().id(1L).iban(IBAN).customerId(42L)
                .balance(new BigDecimal("100.00")).currency("TRY").status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 30))
                .updatedAt(LocalDateTime.of(2026, 1, 2, 12, 45)).build();
    }

    @Test
    void cannotCreateOrListAccountsForAnotherCustomer() {
        assertThatThrownBy(() -> service.createAccount(new CreateAccountRequest(99L, "TRY", BigDecimal.ZERO), 42L))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
        assertThatThrownBy(() -> service.getAccountsByCustomerId(99L, 42L))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
        verifyNoInteractions(repository);
    }

    @Test
    void onlyOwnerCanReadAccountByIban() {
        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account()));
        assertThatThrownBy(() -> service.getAccountByIban(IBAN, 99L))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
        assertThat(service.getAccountByIban(IBAN, 42L).customerId()).isEqualTo(42L);
    }
}
