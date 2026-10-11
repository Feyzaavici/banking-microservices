package org.example.accountservice.controller;

import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.enums.AccountStatus;
import org.example.accountservice.exception.BaseException;
import org.example.accountservice.exception.GlobalExceptionHandler;
import org.example.accountservice.exception.MessageType;
import org.example.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {
    @Mock
    private AccountService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AccountController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void validCreateRequestReturnsAccount() throws Exception {
        when(service.createAccount(any(), eq(42L))).thenReturn(new AccountResponse(1L, "TR000000100000000000000001",
                42L, new BigDecimal("125.50"), "TRY", AccountStatus.ACTIVE, null, null));
        mvc.perform(post("/api/accounts").header("X-Customer-Id", 42L).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":42,"currency":"TRY","initialBalance":125.50}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(125.50));
        verify(service).createAccount(new CreateAccountRequest(42L, "TRY", new BigDecimal("125.50")), 42L);
    }

    @Test
    void missingRequiredFieldsReturnsValidationErrorsWithoutCallingService() throws Exception {
        mvc.perform(post("/api/accounts").header("X-Customer-Id", 42L).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/accounts"))
                .andExpect(jsonPath("$.validationErrors.customerId").isArray())
                .andExpect(jsonPath("$.validationErrors.currency").isArray())
                .andExpect(jsonPath("$.validationErrors.initialBalance").isArray());
        verifyNoInteractions(service);
    }

    @Test
    void unsupportedCurrencyIsRejected() throws Exception {
        mvc.perform(post("/api/accounts").header("X-Customer-Id", 42L).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":42,"currency":"GBP","initialBalance":100}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.currency[0]")
                        .value("Currency must be TRY, USD or EUR"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @EnumSource(MessageType.class)
    void businessErrorsUseMessageTypeStatusAndMessage(MessageType type) throws Exception {
        when(service.getAccountByIban("test-iban", 42L)).thenThrow(new BaseException(type));
        mvc.perform(get("/api/accounts/test-iban").header("X-Customer-Id", 42L))
                .andExpect(status().is(type.getStatus().value()))
                .andExpect(jsonPath("$.status").value(type.getStatus().value()))
                .andExpect(jsonPath("$.message").value(type.getMessage()))
                .andExpect(jsonPath("$.path").value("/api/accounts/test-iban"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/accounts").header("X-Customer-Id", 42L).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(service);
    }

    @Test
    void unsupportedHttpMethodRemains405() throws Exception {
        mvc.perform(delete("/api/accounts"))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.status").value(405));
        verifyNoInteractions(service);
    }

    @Test
    void unexpectedErrorDoesNotExposeInternalMessage() throws Exception {
        when(service.getAccountByIban("test-iban", 42L))
                .thenThrow(new IllegalStateException("Internal database connection details"));
        mvc.perform(get("/api/accounts/test-iban").header("X-Customer-Id", 42L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.status").value(500));
    }
}
