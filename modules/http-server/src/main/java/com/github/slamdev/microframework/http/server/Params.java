package com.github.slamdev.microframework.http.server;

import io.undertow.server.HttpServerExchange;
import lombok.experimental.UtilityClass;

import java.util.Deque;
import java.util.Optional;
import java.util.function.Supplier;

import static io.undertow.util.StatusCodes.UNPROCESSABLE_ENTITY;

@UtilityClass
public class Params {

    public static Optional<String> query(HttpServerExchange exchange, String name) {
        return Optional
                .ofNullable(exchange.getQueryParameters().get(name))
                .map(Deque::getFirst)
                .map(String::trim)
                .map(s -> s.isEmpty() ? null : s);
    }

    public static <X extends Throwable> String requireQuery(HttpServerExchange exchange, String name, Supplier<? extends X> exceptionSupplier) throws X {
        return query(exchange, name).orElseThrow(exceptionSupplier);
    }

    public static String requireQuery(HttpServerExchange exchange, String name, String message) {
        return requireQuery(exchange, name, () -> new HttpException(UNPROCESSABLE_ENTITY, message));
    }
}
