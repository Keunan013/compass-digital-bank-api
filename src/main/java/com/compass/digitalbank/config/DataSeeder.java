package com.compass.digitalbank.config;

import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import com.compass.digitalbank.domain.port.out.PasswordHasher;
import com.compass.digitalbank.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final List<SeedUser> SEED_USERS = List.of(
            new SeedUser("User One", "user1@example.com", Role.USER, "Main Account", new BigDecimal("1000.00")),
            new SeedUser("User Two", "user2@example.com", Role.USER, "Main Account", new BigDecimal("500.00")),
            new SeedUser("Admin", "admin@example.com", Role.ADMIN, "Operations Account", new BigDecimal("0.00")));

    private final SeedProperties seedProperties;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedProperties.enabled()) {
            return;
        }
        String passwordHash = passwordHasher.hash(seedProperties.defaultPassword());
        SEED_USERS.stream()
                .filter(seed -> !userRepository.existsByEmail(seed.email()))
                .forEach(seed -> seedUser(seed, passwordHash));
    }

    private void seedUser(SeedUser seed, String passwordHash) {
        User user = userRepository.save(User.create(seed.name(), seed.email(), passwordHash, seed.role()));
        Account account = accountRepository.save(Account.open(user.getId(), seed.accountName(), seed.initialBalance()));
        log.info("Seeded user {} with account {}", user.getEmail(), account.getId());
    }

    private record SeedUser(String name, String email, Role role, String accountName, BigDecimal initialBalance) {
    }
}
