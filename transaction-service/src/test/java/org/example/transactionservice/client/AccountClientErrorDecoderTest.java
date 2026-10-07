package org.example.transactionservice.client;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.example.transactionservice.exception.AccountNotFoundException;
import org.example.transactionservice.exception.MessageType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountClientErrorDecoderTest {

    private final AccountClientErrorDecoder decoder = new AccountClientErrorDecoder();

    @Test
    void converts404ToAccountNotFoundException(){
        Exception result = decoder.decode("AccountClient#getAccountByIban(String)", response(404));

        assertThat(result)
                .isInstanceOfSatisfying(
                        AccountNotFoundException.class,
                        exception -> {
                            assertThat(exception.getMessageType())
                                    .isEqualTo(MessageType.ACCOUNT_NOT_FOUND);

                            assertThat(exception.getCause())
                                    .isInstanceOf(FeignException.NotFound.class);
                        }
                );
    }

    @Test
    void preservesDefaultExceptionFor500() {
        Exception result = decoder.decode(
                "AccountClient#getAccountByIban(String)",
                response(500)
        );

        assertThat(result)
                .isInstanceOf(FeignException.InternalServerError.class);
    }

    private Response response(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://account-service/api/accounts/iban/test-iban",
                Map.of(),
                (byte[]) null,
                StandardCharsets.UTF_8,
                null
        );

        return Response.builder()
                .status(status)
                .reason("Test response")
                .request(request)
                .headers(Map.of())
                .build();
    }
}
