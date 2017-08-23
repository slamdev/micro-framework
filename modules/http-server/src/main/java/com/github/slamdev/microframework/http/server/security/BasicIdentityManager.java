package com.github.slamdev.microframework.http.server.security;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class BasicIdentityManager implements IdentityManager {

    private final Function<String, Account> verifier;

    @Override
    public Account verify(Account account) {
        return account;
    }

    @Override
    public Account verify(String id, Credential credential) {
        return verifier.apply(id);
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }
}
