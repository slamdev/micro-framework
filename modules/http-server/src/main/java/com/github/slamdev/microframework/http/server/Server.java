package com.github.slamdev.microframework.http.server;

import com.github.slamdev.microframework.http.server.handlers.ShutdownHandler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

import static com.github.slamdev.microframework.http.server.HandlerFactory.*;
import static java.util.Optional.ofNullable;

public class Server {

    private final Config appConfig;

    private Undertow undertow;

    public Server(Config appConfig) {
        this.appConfig = appConfig;
    }

    public void start(HttpHandler baseHandler) {
        Config config = appConfig.withFallback(ConfigFactory.load("reference.properties"));
        HttpHandler handler = configHandler(
                loggingHandler(
                        healthHandler(
                                shutdownHandler(baseHandler, config.getString("endpoints.shutdown.path")),
                                config.getString("endpoints.health.path"))
                ), config);
        undertow = Undertow.builder()
                .addHttpListener(config.getInt("server.port"), "0.0.0.0")
                .setHandler(handler).build();
        ShutdownHandler.setServerInstance(undertow);
        undertow.start();
    }

    public void stop() {
        ofNullable(undertow).ifPresent(Undertow::stop);
    }
}
