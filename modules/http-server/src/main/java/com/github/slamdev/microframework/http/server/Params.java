package com.github.slamdev.microframework.http.server;

import io.undertow.server.HttpServerExchange;
import lombok.experimental.UtilityClass;

import java.util.Deque;
import java.util.Optional;
import java.util.OptionalLong;
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

    public static OptionalLong queryLong(HttpServerExchange exchange, String name) {
        return query(exchange, name)
                .map(Long::parseLong)
                .map(OptionalLong::of)
                .orElseGet(OptionalLong::empty);
    }

    public static <X extends Throwable> long requireQueryLong(HttpServerExchange exchange, String name, Supplier<? extends X> exceptionSupplier) throws X {
        return queryLong(exchange, name).orElseThrow(exceptionSupplier);
    }

    public static long requireQueryLong(HttpServerExchange exchange, String name, String message) {
        return requireQueryLong(exchange, name, () -> new HttpException(UNPROCESSABLE_ENTITY, message));
    }

    public static <K extends Enum<K>> Optional<K> queryEnum(HttpServerExchange exchange, String name, Class<K> type) {
        return Optional
                .ofNullable(exchange.getQueryParameters().get(name))
                .map(Deque::getFirst)
                .map(String::trim)
                .map(s -> s.isEmpty() ? null : s)
                .map(v -> K.valueOf(type, v));
    }

    public static <X extends Throwable, K extends Enum<K>> K requireQueryEnum(HttpServerExchange exchange, String name, Class<K> type, Supplier<? extends X> exceptionSupplier) throws X {
        return queryEnum(exchange, name, type).orElseThrow(exceptionSupplier);
    }

    public static <K extends Enum<K>> K requireQueryEnum(HttpServerExchange exchange, String name, Class<K> type, String message) {
        return requireQueryEnum(exchange, name, type, () -> new HttpException(UNPROCESSABLE_ENTITY, message));
    }
}
