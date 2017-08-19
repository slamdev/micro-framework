package com.github.slamdev.microframework.http.server;

import com.github.slamdev.microframework.http.server.handlers.DependencyInjectionHandler;
import com.github.slamdev.microframework.http.server.handlers.LogbackAccessHandler;
import com.github.slamdev.microframework.http.server.handlers.ShutdownHandler;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.util.AttachmentKey;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class HandlerFactory {

    public AttachmentKey<Config> CONFIG = AttachmentKey.create(Config.class);

    public LogbackAccessHandler loggingHandler(HttpHandler next) {
        return new LogbackAccessHandler(next);
    }

    public DependencyInjectionHandler<Config> configHandler(HttpHandler nextHandler, Config config) {
        return new DependencyInjectionHandler<>(nextHandler, config, CONFIG);
    }

    public HttpHandler healthHandler(HttpHandler nextHandler, String path) {
        return Handlers.path(nextHandler)
                .addExactPath(path, exchange -> exchange.getResponseSender().send("OK"));
    }

    public HttpHandler shutdownHandler(HttpHandler nextHandler, String path) {
        GracefulShutdownHandler handler = new ShutdownHandler(nextHandler);
        return Handlers.path(handler).addExactPath(path, exchange -> {
            handler.shutdown();
            handler.awaitShutdown();
        });
    }
}
