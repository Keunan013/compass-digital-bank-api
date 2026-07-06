package com.compass.digitalbank.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final Instant createdAt;

    private User(UUID id, String name, String email, String passwordHash, Role role, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User register(String name, String email, String passwordHash) {
        return create(name, email, passwordHash, Role.USER);
    }

    public static User create(String name, String email, String passwordHash, Role role) {
        return new User(null, name, email, passwordHash, role, null);
    }

    public static User restore(UUID id, String name, String email, String passwordHash, Role role, Instant createdAt) {
        return new User(id, name, email, passwordHash, role, createdAt);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
