package org.example.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long customerId, String tcNo) {
        Date now = new Date();
        return Jwts.builder().subject(tcNo).claim("customerId", customerId)
                .issuedAt(now).expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
    }

    public Long validateAndExtractCustomerId(String token) {
        Claims claims = Jwts.parser().verifyWith(key)
                .build().parseSignedClaims(token).getPayload();
        Long customerId = claims.get("customerId", Long.class);
        if (customerId == null || customerId <= 0 || claims.getExpiration() == null) {
            throw new JwtException("Invalid customer token");
        }
        return customerId;
    }
}
