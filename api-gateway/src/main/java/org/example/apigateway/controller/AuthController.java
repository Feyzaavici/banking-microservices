package org.example.apigateway.controller;

import org.example.apigateway.dto.CustomerAuthResponse;
import org.example.apigateway.dto.LoginRequest;
import org.example.apigateway.dto.LoginResponse;
import org.example.apigateway.dto.RegisterRequest;
import org.example.apigateway.exception.DownstreamException;
import org.example.apigateway.exception.ErrorResponse;
import org.example.apigateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebClient.Builder webClientBuilder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest request){
        return webClientBuilder.build()
                .post()
                .uri("lb://customer-service/api/customers/authenticate")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::toDownstreamException)
                .onStatus(HttpStatusCode::is5xxServerError, this::toDownstreamException)
                .bodyToMono(CustomerAuthResponse.class)
                .map(customer -> new LoginResponse(
                        jwtService.generateToken(customer.getId(), customer.getTcNo())
                ));
    }

    @PostMapping("/register")
    public Mono<LoginResponse> register(@RequestBody RegisterRequest request) {
        return webClientBuilder.build()
                .post()
                .uri("lb://customer-service/api/customers")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::toDownstreamException)
                .onStatus(HttpStatusCode::is5xxServerError, this::toDownstreamException)
                .bodyToMono(CustomerAuthResponse.class)
                .map(customer -> new LoginResponse(
                        jwtService.generateToken(customer.getId(), customer.getTcNo())
                ));
    }

    private Mono<? extends Throwable> toDownstreamException(ClientResponse response) {
        return response.bodyToMono(ErrorResponse.class)
                .map(err -> new DownstreamException(response.statusCode(), err.message()))
                .defaultIfEmpty(new DownstreamException(response.statusCode(), "customer-service request failed"));
    }

}
