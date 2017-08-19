package com.github.slamdev.microframework.http.server;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class HttpException extends RuntimeException {

    int status;
    String message;

    public HttpException(int status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
