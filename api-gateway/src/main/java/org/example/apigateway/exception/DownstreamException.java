package org.example.apigateway.exception;

import org.springframework.http.HttpStatusCode;

public class DownstreamException extends RuntimeException {

    private final HttpStatusCode status;

    public DownstreamException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

}
