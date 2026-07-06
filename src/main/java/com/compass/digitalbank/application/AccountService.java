package com.compass.digitalbank.application;

import com.compass.digitalbank.domain.exception.ResourceNotFoundException;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.in.AccountUseCase;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements AccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account create(CreateAccountCommand command) {
        Account account = accountRepository.save(
                Account.open(command.ownerId(), command.name(), command.initialBalance()));
        log.info("Opened account {} for owner {}", account.getId(), command.ownerId());
        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getById(UUID accountId, Requester requester) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        requester.requireCanActOnBehalfOf(account.getOwnerId());
        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Account> listOwnedBy(UUID ownerId, PageQuery pageQuery) {
        return accountRepository.findByOwnerId(ownerId, pageQuery);
    }
}
