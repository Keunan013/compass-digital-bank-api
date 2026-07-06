package com.compass.digitalbank.domain.model;

import com.compass.digitalbank.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account accountWithBalance(String balance) {
        return Account.restore(UUID.randomUUID(), UUID.randomUUID(), "Checking",
                new BigDecimal(balance), true, Instant.now());
    }

    @Test
    void debitReducesBalance() {
        Account account = accountWithBalance("100.00");

        account.debit(new BigDecimal("30.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("70.00");
    }

    @Test
    void creditIncreasesBalance() {
        Account account = accountWithBalance("100.00");

        account.credit(new BigDecimal("25.50"));

        assertThat(account.getBalance()).isEqualByComparingTo("125.50");
    }

    @Test
    void debitBeyondBalanceIsRejected() {
        Account account = accountWithBalance("40.00");

        assertThatThrownBy(() -> account.debit(new BigDecimal("40.01")))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("40.00");
    }

    @Test
    void debitEntireBalanceIsAllowed() {
        Account account = accountWithBalance("40.00");

        account.debit(new BigDecimal("40.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void ownershipIsCheckedById() {
        UUID ownerId = UUID.randomUUID();
        Account account = Account.restore(UUID.randomUUID(), ownerId, "Checking",
                new BigDecimal("10.00"), true, Instant.now());

        assertThat(account.isOwnedBy(ownerId)).isTrue();
        assertThat(account.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}
