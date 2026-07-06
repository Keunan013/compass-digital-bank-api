package com.compass.digitalbank.domain.model;

import com.compass.digitalbank.domain.exception.ForbiddenOperationException;

import java.util.UUID;

public record Requester(UUID userId, boolean admin) {

    public void requireCanActOnBehalfOf(UUID ownerId) {
        boolean allowed = admin || userId.equals(ownerId);
        if (!allowed) {
            throw new ForbiddenOperationException("Requester is not allowed to act on this account");
        }
    }
}
