package org.example.apigateway.controller;

import java.util.concurrent.atomic.AtomicReference;
import org.example.apigateway.dto.LoginRequest;
import org.example.apigateway.exception.DownstreamException;
import org.example.apigateway.dto.RegisterRequest;
import org.example.apigateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthControllerTest {
    private final JwtService jwt = new JwtService("test-only-jwt-secret-at-least-32-bytes-long", 3600000);

    @Test
    void loginUsesCustomerServiceAndReturnsVerifiableToken() {
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        var controller = controller(sent, HttpStatus.OK, "{\"id\":42,\"tcNo\":\"12345678901\"}");
        var response = controller.login(new LoginRequest("12345678901", "password123")).block();
        assertThat(sent.get().url().toString()).isEqualTo("lb://customer-service/api/customers/authenticate");
        assertThat(jwt.validateAndExtractCustomerId(response.token()))
                .isEqualTo(42L);
    }

    @Test
    void registerReadsCustomerResponseAndReturnsCreatedToken() {
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        var controller = controller(sent, HttpStatus.CREATED,
                "{\"id\":42,\"tcNo\":\"12345678901\",\"firstName\":\"Test\"}");
        var response = controller.register(new RegisterRequest("12345678901", "Test", "User", "a@b.com",
                "5551234567", java.time.LocalDate.of(1990, 1, 1), "Address", "password123")).block();
        assertThat(sent.get().url().getPath()).isEqualTo("/api/customers");
        assertThat(jwt.validateAndExtractCustomerId(response.token()))
                .isEqualTo(42L);
    }

    @Test
    void downstreamAuthenticationAndDuplicateErrorsKeepTheirStatus() {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.UNAUTHORIZED, HttpStatus.CONFLICT}) {
            var controller = controller(new AtomicReference<>(), status, """
                    {"timestamp":"2026-10-11T13:00:00","status":%d,"error":"Rejected",
                     "message":"Rejected","path":"/api/customers/authenticate","validationErrors":{}}
                    """.formatted(status.value()));
            assertThatThrownBy(() -> controller.login(new LoginRequest("12345678901", "wrong-password")).block())
                    .isInstanceOfSatisfying(DownstreamException.class, error -> {
                        assertThat(error.getStatus()).isEqualTo(status);
                        assertThat(error.getMessage()).isEqualTo("Rejected");
                    });
        }
    }

    private AuthController controller(AtomicReference<ClientRequest> sent, HttpStatus status, String body) {
        var builder = WebClient.builder()
                .exchangeFunction(request -> {
                    sent.set(request);
                    return Mono.just(ClientResponse.create(status).header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .body(body).build());
                });
        return new AuthController(builder, jwt);
    }
}
