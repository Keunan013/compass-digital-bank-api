package com.compass.digitalbank.adapter.in.web.security;

import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Role;

import java.util.UUID;

public record AppUserPrincipal(UUID id, String email, Role role) {

    public static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + role.name();
    }

    public Requester toRequester() {
        return new Requester(id, role == Role.ADMIN);
    }
}
