package com.compass.digitalbank.adapter.out.persistence;

import com.compass.digitalbank.adapter.out.persistence.entity.AccountEntity;
import com.compass.digitalbank.adapter.out.persistence.mapper.AccountPersistenceMapper;
import com.compass.digitalbank.adapter.out.persistence.repository.AccountJpaRepository;
import com.compass.digitalbank.domain.exception.ResourceNotFoundException;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private static final String CREATED_AT = "createdAt";

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = account.getId() == null
                ? mapper.toNewEntity(account)
                : applyChanges(account);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByIdForUpdate(UUID id) {
        return jpaRepository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Account> findByOwnerId(UUID ownerId, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, CREATED_AT));
        return PersistencePageMapper.toPageResult(jpaRepository.findByOwnerId(ownerId, pageable), mapper::toDomain);
    }

    private AccountEntity applyChanges(Account account) {
        AccountEntity entity = jpaRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        entity.setBalance(account.getBalance());
        entity.setActive(account.isActive());
        return entity;
    }
}
