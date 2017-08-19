package com.github.slamdev.microframework.http.server.handlers;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;

public class ShutdownHandler extends GracefulShutdownHandler {

    private static Undertow server;

    public ShutdownHandler(HttpHandler next) {
        super(next);
    }

    public static void setServerInstance(Undertow server) {
        ShutdownHandler.server = server;
    }

    public void awaitShutdown() throws InterruptedException {
        super.awaitShutdown();
        server.stop();
    }
}
