package com.compass.digitalbank.domain.port.out;

import com.compass.digitalbank.domain.model.User;

public interface TokenProvider {

    IssuedToken issue(User user);

    record IssuedToken(String value, long expiresInSeconds) {
    }
}
