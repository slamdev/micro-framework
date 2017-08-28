package com.github.slamdev.microframework.http.server.security;

import com.github.slamdev.microframework.http.server.HttpException;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

import static io.undertow.util.StatusCodes.UNAUTHORIZED;
import static java.util.Optional.ofNullable;

@UtilityClass
public class SecurityUtils {

    public static boolean hasRole(HttpServerExchange exchange, String... roles) {
        return ofNullable(exchange.getSecurityContext())
                .map(SecurityContext::getAuthenticatedAccount)
                .map(Account::getRoles)
                .orElse(Collections.emptySet())
                .containsAll(Arrays.asList(roles));
    }

    public static <X extends Throwable> void requireRole(HttpServerExchange exchange, Supplier<? extends X> exceptionSupplier, String... roles) throws X {
        if (!hasRole(exchange, roles)) {
            throw exceptionSupplier.get();
        }
    }

    public static void requireRole(String message, HttpServerExchange exchange, String... roles) {
        if (!hasRole(exchange, roles)) {
            throw new HttpException(UNAUTHORIZED, message);
        }
    }
}
