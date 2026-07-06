package com.compass.digitalbank.domain.model;

import com.compass.digitalbank.domain.exception.InsufficientBalanceException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Account {

    private final UUID id;
    private final UUID ownerId;
    private final String name;
    private BigDecimal balance;
    private final boolean active;
    private final Instant createdAt;

    private Account(UUID id, UUID ownerId, String name, BigDecimal balance, boolean active, Instant createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.balance = balance;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static Account open(UUID ownerId, String name, BigDecimal initialBalance) {
        return new Account(null, ownerId, name, initialBalance, true, null);
    }

    public static Account restore(UUID id, UUID ownerId, String name, BigDecimal balance,
                                  boolean active, Instant createdAt) {
        return new Account(id, ownerId, name, balance, active, createdAt);
    }

    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }
}
