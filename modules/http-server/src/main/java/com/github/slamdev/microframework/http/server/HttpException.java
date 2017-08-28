package com.github.slamdev.microframework.http.server;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class HttpException extends RuntimeException {

    int status;

    String statusMessage;

    public HttpException(int status, String statusMessage) {
        super(String.join(" ", Integer.toString(status), statusMessage));
        this.status = status;
        this.statusMessage = statusMessage;
    }
}
