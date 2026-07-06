package com.compass.digitalbank.adapter.out.persistence;

import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.in.TransferUseCase;
import com.compass.digitalbank.domain.port.in.TransferUseCase.TransferCommand;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import com.compass.digitalbank.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "app.rate-limit.enabled=false",
        "app.messaging.enabled=false"
})
class TransferConcurrencyIT {

    private static final int THREAD_POOL_SIZE = 16;
    private static final int TRANSFER_COUNT = 100;
    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransferUseCase transferUseCase;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void concurrentTransfersNeverLoseUpdatesOrOverdraw() throws Exception {
        User owner = userRepository.save(User.create("Owner", uniqueEmail(), "hash", Role.USER));
        Account source = accountRepository.save(Account.open(owner.getId(), "Source", INITIAL_BALANCE));
        Account destination = accountRepository.save(Account.open(owner.getId(), "Destination", BigDecimal.ZERO));
        Requester requester = new Requester(owner.getId(), false);

        runConcurrently(TRANSFER_COUNT, () -> {
            transferUseCase.transfer(new TransferCommand(
                    requester, source.getId(), destination.getId(), TRANSFER_AMOUNT, null));
            return null;
        });

        BigDecimal sourceBalance = balanceOf(source.getId());
        BigDecimal destinationBalance = balanceOf(destination.getId());

        assertThat(sourceBalance).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(destinationBalance).isEqualByComparingTo(INITIAL_BALANCE);
        assertThat(sourceBalance.add(destinationBalance)).isEqualByComparingTo(INITIAL_BALANCE);
    }

    @Test
    void concurrentBidirectionalTransfersConserveTotalWithoutDeadlock() throws Exception {
        User owner = userRepository.save(User.create("Owner", uniqueEmail(), "hash", Role.USER));
        Account left = accountRepository.save(Account.open(owner.getId(), "Left", INITIAL_BALANCE));
        Account right = accountRepository.save(Account.open(owner.getId(), "Right", INITIAL_BALANCE));
        Requester requester = new Requester(owner.getId(), false);
        BigDecimal total = INITIAL_BALANCE.add(INITIAL_BALANCE);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < TRANSFER_COUNT; i++) {
            boolean leftToRight = i % 2 == 0;
            UUID from = leftToRight ? left.getId() : right.getId();
            UUID to = leftToRight ? right.getId() : left.getId();
            tasks.add(() -> {
                transferUseCase.transfer(new TransferCommand(requester, from, to, TRANSFER_AMOUNT, null));
                return null;
            });
        }
        runAll(tasks);

        assertThat(balanceOf(left.getId()).add(balanceOf(right.getId()))).isEqualByComparingTo(total);
    }

    private String uniqueEmail() {
        return "owner-" + UUID.randomUUID() + "@example.com";
    }

    private BigDecimal balanceOf(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow().getBalance();
    }

    private void runConcurrently(int count, Callable<Void> task) throws Exception {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(task);
        }
        runAll(tasks);
    }

    private void runAll(List<Callable<Void>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return task.call();
                }));
            }
            startGate.countDown();
            for (Future<Void> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
