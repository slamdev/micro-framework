package com.github.slamdev.microframework.http.server.security;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.ExternalCredential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormParserFactory;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static io.undertow.security.api.AuthenticationMechanism.AuthenticationMechanismOutcome.*;
import static io.undertow.security.api.AuthenticationMechanism.ChallengeResult.NOT_SENT;

@RequiredArgsConstructor
public class HeaderAuthenticationMechanism implements AuthenticationMechanism {

    public static final String NAME = "HEADER";

    public static final String HEADER_PARAM = "HEADER";

    private final String name;

    private final IdentityManager identityManager;

    private final String headerName;

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        String principal = exchange.getRequestHeaders().getFirst(headerName);
        if (principal == null) {
            return NOT_ATTEMPTED;
        }
        Account account = identityManager.verify(principal, ExternalCredential.INSTANCE);
        if (account == null) {
            return NOT_AUTHENTICATED;
        }
        securityContext.authenticationComplete(account, name, false);
        return AUTHENTICATED;
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        return NOT_SENT;
    }

    @RequiredArgsConstructor
    public static final class Factory implements AuthenticationMechanismFactory {

        private final IdentityManager identityManager;

        @Override
        public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory, Map<String, String> properties) {
            return new HeaderAuthenticationMechanism(mechanismName, identityManager, properties.get(HEADER_PARAM));
        }
    }
}
