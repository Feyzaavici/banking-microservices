package org.example.customerservice.service;

import java.time.LocalDate;
import java.util.Optional;
import org.example.customerservice.dto.AuthenticateRequest;
import org.example.customerservice.dto.CreateCustomerRequest;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.enums.CustomerStatus;
import org.example.customerservice.exception.BaseException;
import org.example.customerservice.exception.MessageType;
import org.example.customerservice.mapper.CustomerMapper;
import org.example.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAuthenticationTest {
    private final CustomerRepository repository = mock(CustomerRepository.class);
    private final CustomerMapper mapper = mock(CustomerMapper.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final CustomerService service = new CustomerService(repository, mapper, encoder);
    private Customer customer;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setId(42L);
        customer.setTcNo("12345678901");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setPassword(encoder.encode("password123"));
    }

    @Test
    void registrationStoresOnlyPasswordHash() {
        var request = new CreateCustomerRequest("12345678901", "Test", "User", "test@example.com",
                "5551234567", LocalDate.of(1990, 1, 1), "Address", "password123");
        when(mapper.toEntity(request)).thenReturn(customer);
        when(repository.save(customer)).thenReturn(customer);
        service.createCustomer(request);
        assertThat(customer.getPassword()).isNotEqualTo(request.password());
        assertThat(encoder.matches(request.password(), customer.getPassword())).isTrue();
        verify(repository).save(customer);
    }

    @Test
    void validCredentialsReturnCustomerIdentity() {
        when(repository.findByTcNo(customer.getTcNo())).thenReturn(Optional.of(customer));
        assertThat(service.authenticate(new AuthenticateRequest(customer.getTcNo(), "password123")).getId()).isEqualTo(42L);
    }

    @Test
    void wrongPasswordUnknownAndLegacyCustomerAreUnauthorized() {
        when(repository.findByTcNo(customer.getTcNo())).thenReturn(Optional.of(customer));
        assertUnauthorized("wrong-password");
        customer.setPassword(null);
        assertUnauthorized("password123");
        when(repository.findByTcNo(customer.getTcNo())).thenReturn(Optional.empty());
        assertUnauthorized("password123");
    }

    @Test
    void anotherCustomersProfileIsForbidden() {
        assertThatThrownBy(() -> service.getCustomer(99L, 42L))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
        verifyNoInteractions(repository);
        when(repository.findByTcNo(customer.getTcNo())).thenReturn(Optional.of(customer));
        assertThatThrownBy(() -> service.getCustomer(customer.getTcNo(), 99L))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.ACCESS_DENIED));
    }

    private void assertUnauthorized(String password) {
        assertThatThrownBy(() -> service.authenticate(new AuthenticateRequest(customer.getTcNo(), password)))
                .isInstanceOfSatisfying(BaseException.class,
                        error -> assertThat(error.getMessageType()).isEqualTo(MessageType.INVALID_CREDENTIALS));
    }
}
