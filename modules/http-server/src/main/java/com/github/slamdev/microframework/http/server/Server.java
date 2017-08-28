package com.github.slamdev.microframework.http.server;

import com.github.slamdev.microframework.http.server.handlers.ShutdownHandler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.slamdev.microframework.http.server.HandlerFactory.*;
import static io.undertow.Handlers.exceptionHandler;
import static io.undertow.server.handlers.ExceptionHandler.THROWABLE;
import static io.undertow.util.StatusCodes.INTERNAL_SERVER_ERROR;
import static java.util.Optional.ofNullable;

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final Config appConfig;

    private Undertow undertow;

    private static void handleInternalException(HttpServerExchange exchange) {
        Throwable throwable = exchange.getAttachment(THROWABLE);
        LOGGER.error("Exception handler caught an error", throwable);
        Responders.sendError(exchange, INTERNAL_SERVER_ERROR, throwable);
    }

    private static void handleHttpException(HttpServerExchange exchange) {
        Throwable throwable = exchange.getAttachment(THROWABLE);
        if (throwable instanceof HttpException) {
            Responders.sendError(exchange, ((HttpException) throwable).getStatus(), throwable);
        } else {
            handleInternalException(exchange);
        }
    }

    public Server(Config appConfig) {
        this.appConfig = appConfig;
    }

    public void start(HttpHandler baseHandler) {
        Config config = appConfig.withFallback(ConfigFactory.load("reference.properties"));
        LOGGER.info("{}", config);
        HttpHandler handler = configHandler(
                loggingHandler(
                        exceptionHandler(
                                healthHandler(
                                        shutdownHandler(baseHandler, config.getString("endpoints.shutdown.path")),
                                        config.getString("endpoints.health.path"))
                        )
                                .addExceptionHandler(HttpException.class, Server::handleHttpException)
                                .addExceptionHandler(Throwable.class, Server::handleInternalException)
                ), config);
        undertow = Undertow.builder()
                .addHttpListener(config.getInt("server.port"), "localhost")
                .setHandler(handler).build();
        ShutdownHandler.setServerInstance(undertow);
        undertow.start();
    }

    public void stop() {
        ofNullable(undertow).ifPresent(Undertow::stop);
    }
}
