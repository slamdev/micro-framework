package com.github.slamdev.microframework.http.server.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DependencyInjectionHandler<T> implements HttpHandler {

    private final HttpHandler nextHandler;

    private final T dependency;

    private final AttachmentKey<T> attachmentKey;

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(attachmentKey, dependency);
        nextHandler.handleRequest(exchange);
    }
}
