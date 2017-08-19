package com.github.slamdev.microframework.http.server.handlers;

import com.typesafe.config.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigHandler implements HttpHandler {

    public static final AttachmentKey<Config> CONFIG = AttachmentKey.create(Config.class);

    private final HttpHandler nextHandler;

    private final Config config;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(CONFIG, config);
        nextHandler.handleRequest(exchange);
    }
}
