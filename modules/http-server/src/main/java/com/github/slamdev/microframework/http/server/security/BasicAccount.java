package com.github.slamdev.microframework.http.server.security;

import io.undertow.security.idm.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.security.Principal;
import java.util.Set;

@Builder
@Getter
public class BasicAccount implements Account {

    private final Principal principal;

    @Singular
    private final Set<String> roles;
}
