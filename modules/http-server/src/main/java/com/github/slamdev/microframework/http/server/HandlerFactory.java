package com.github.slamdev.microframework.http.server;

import com.github.slamdev.microframework.http.server.handlers.ConfigHandler;
import com.github.slamdev.microframework.http.server.handlers.LogbackAccessHandler;
import com.github.slamdev.microframework.http.server.handlers.ShutdownHandler;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;

public final class HandlerFactory {

    private HandlerFactory() {
        // Utility class
    }

    public static LogbackAccessHandler loggingHandler(HttpHandler next) {
        return new LogbackAccessHandler(next);
    }

    public static ConfigHandler configHandler(HttpHandler nextHandler, Config config) {
        return new ConfigHandler(nextHandler, config);
    }

    public static HttpHandler healthHandler(HttpHandler nextHandler, String path) {
        return Handlers.path(nextHandler)
                .addExactPath(path, exchange -> exchange.getResponseSender().send("OK"));
    }

    public static HttpHandler shutdownHandler(HttpHandler nextHandler, String path) {
        GracefulShutdownHandler handler = new ShutdownHandler(nextHandler);
        return Handlers.path(handler).addExactPath(path, exchange -> {
            handler.shutdown();
            handler.awaitShutdown();
        });
    }
}
