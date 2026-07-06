package com.compass.digitalbank.application;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import com.compass.digitalbank.domain.exception.InactiveAccountException;
import com.compass.digitalbank.domain.exception.InvalidTransferException;
import com.compass.digitalbank.domain.exception.ResourceNotFoundException;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.in.TransferUseCase;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import com.compass.digitalbank.domain.port.out.DomainEventPublisher;
import com.compass.digitalbank.domain.port.out.TransferRepository;
import com.compass.digitalbank.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 80;

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Transfer transfer(TransferCommand command) {
        validate(command);

        Optional<Transfer> replayed = replayIfIdempotent(command.idempotencyKey());
        if (replayed.isPresent()) {
            log.info("Idempotent replay for key {} returned transfer {}",
                    command.idempotencyKey(), replayed.get().getId());
            return replayed.get();
        }

        LockedPair accounts = lockInDeadlockSafeOrder(command.sourceAccountId(), command.destinationAccountId());
        Account source = accounts.source();
        Account destination = accounts.destination();
        command.requester().requireCanActOnBehalfOf(source.getOwnerId());
        ensureBothActive(source, destination);

        source.debit(command.amount());
        destination.credit(command.amount());
        accountRepository.save(source);
        accountRepository.save(destination);

        Transfer transfer = transferRepository.save(Transfer.completed(
                source.getId(), destination.getId(), command.amount(), command.idempotencyKey()));

        publishCompletion(transfer, source);
        log.info("Transfer {} of {} completed from {} to {}",
                transfer.getId(), command.amount(), source.getId(), destination.getId());
        return transfer;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Transfer> listAccountTransactions(UUID accountId, Requester requester, PageQuery pageQuery) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        requester.requireCanActOnBehalfOf(account.getOwnerId());
        return transferRepository.findByAccountId(accountId, pageQuery);
    }

    private void validate(TransferCommand command) {
        if (command.amount() == null || command.amount().signum() <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero");
        }
        if (command.sourceAccountId().equals(command.destinationAccountId())) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }
        if (StringUtils.hasText(command.idempotencyKey())
                && command.idempotencyKey().length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new InvalidTransferException(
                    "Idempotency-Key must be at most " + MAX_IDEMPOTENCY_KEY_LENGTH + " characters");
        }
    }

    private Optional<Transfer> replayIfIdempotent(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return Optional.empty();
        }
        return transferRepository.findByIdempotencyKey(idempotencyKey);
    }

    private LockedPair lockInDeadlockSafeOrder(UUID sourceId, UUID destinationId) {
        List<UUID> ordered = orderById(sourceId, destinationId);
        Account first = lockAccount(ordered.get(0));
        Account second = lockAccount(ordered.get(1));
        Account source = first.getId().equals(sourceId) ? first : second;
        Account destination = source == first ? second : first;
        return new LockedPair(source, destination);
    }

    private List<UUID> orderById(UUID left, UUID right) {
        return left.compareTo(right) <= 0 ? List.of(left, right) : List.of(right, left);
    }

    private Account lockAccount(UUID accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void ensureBothActive(Account source, Account destination) {
        if (!source.isActive() || !destination.isActive()) {
            throw new InactiveAccountException();
        }
    }

    private void publishCompletion(Transfer transfer, Account source) {
        User recipient = userRepository.findById(source.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Account owner not found"));
        eventPublisher.publish(new TransferCompletedEvent(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount(),
                source.getBalance(),
                recipient.getName(),
                recipient.getEmail(),
                transfer.getCreatedAt() != null ? transfer.getCreatedAt() : Instant.now()));
    }

    private record LockedPair(Account source, Account destination) {
    }
}
