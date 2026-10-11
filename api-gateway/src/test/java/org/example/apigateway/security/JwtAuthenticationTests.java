package org.example.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAuthenticationTests {
    private static final String SECRET = "test-only-jwt-secret-at-least-32-bytes-long";
    private final JwtService jwt = new JwtService(SECRET, 3600000);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt);

    @Test
    void validTokenCarriesCustomerIdentity() {
        assertThat(jwt.validateAndExtractCustomerId(jwt.generateToken(42L, "12345678901"))).isEqualTo(42L);
    }

    @Test
    void rejectsExpiredTokensAndTokensSignedWithAnotherKey() {
        String expired = Jwts.builder().claim("customerId", 42L)
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertThatThrownBy(() -> jwt.validateAndExtractCustomerId(expired))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        String forged = new JwtService("another-test-secret-at-least-32-bytes-long", 3600000)
                .generateToken(42L, "12345678901");
        assertThatThrownBy(() -> jwt.validateAndExtractCustomerId(forged))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void forwardsVerifiedCustomerIdentity() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/accounts/customer/42")
                .header("Authorization", "Bearer " + jwt.generateToken(42L, "12345678901"))
                .header("X-Customer-Id", "99"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        filter.filter(exchange, value -> { forwarded.set(value); return Mono.empty(); }).block();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-Customer-Id")).isEqualTo("42");
    }

    @Test
    void missingOrInvalidBearerTokenIsUnauthorized() {
        for (String token : new String[]{"", "Bearer invalid"}) {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/accounts/customer/42")
                    .header("Authorization", token));
            filter.filter(exchange, value -> { throw new AssertionError("Must not forward"); }).block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void loginAndRegistrationDoNotRequireToken() {
        for (String path : new String[]{"/auth/login", "/auth/register"}) {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));
            AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
            filter.filter(exchange, value -> { forwarded.set(value); return Mono.empty(); }).block();
            assertThat(forwarded.get()).isNotNull();
        }
    }
}
