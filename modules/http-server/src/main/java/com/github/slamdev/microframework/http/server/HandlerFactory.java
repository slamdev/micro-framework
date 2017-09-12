package com.github.slamdev.microframework.http.server;

import com.github.slamdev.microframework.http.server.handlers.DependencyInjectionHandler;
import com.github.slamdev.microframework.http.server.handlers.LogbackAccessHandler;
import com.github.slamdev.microframework.http.server.handlers.ShutdownHandler;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.util.AttachmentKey;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static io.undertow.security.api.AuthenticationMode.PRO_ACTIVE;
import static io.undertow.server.handlers.ExceptionHandler.THROWABLE;
import static io.undertow.util.StatusCodes.INTERNAL_SERVER_ERROR;
import static java.util.Arrays.asList;

@Slf4j
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

    public HttpHandler securityHandler(HttpHandler toWrap, IdentityManager identityManager, AuthenticationMechanism... mechanisms) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        handler = new AuthenticationMechanismsHandler(handler, asList(mechanisms));
        handler = new SecurityInitialHandler(PRO_ACTIVE, identityManager, handler);
        return handler;
    }

    public HttpHandler jsonExceptionHandler(HttpHandler toWrap) {
        return Handlers.exceptionHandler(toWrap)
                .addExceptionHandler(HttpException.class, HandlerFactory::handleHttpException)
                .addExceptionHandler(Throwable.class, HandlerFactory::handleInternalException);
    }

    private void handleInternalException(HttpServerExchange exchange) {
        Throwable throwable = exchange.getAttachment(THROWABLE);
        log.error("Exception handler caught an error", throwable);
        Responders.sendError(exchange, INTERNAL_SERVER_ERROR, throwable);
    }

    private void handleHttpException(HttpServerExchange exchange) {
        Throwable throwable = exchange.getAttachment(THROWABLE);
        if (throwable instanceof HttpException) {
            Responders.sendError(exchange, ((HttpException) throwable).getStatus(), throwable);
        } else {
            handleInternalException(exchange);
        }
    }
}
