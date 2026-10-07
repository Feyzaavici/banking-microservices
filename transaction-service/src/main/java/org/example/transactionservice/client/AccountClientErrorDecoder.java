package org.example.transactionservice.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.example.transactionservice.exception.AccountNotFoundException;

@RequiredArgsConstructor
public class AccountClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default() ;

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception originalException = defaultDecoder.decode(methodKey, response);
        if(response.status() == 404){
            return new AccountNotFoundException(originalException);
        }
        return originalException;
    }
}
