package com.compass.digitalbank.adapter.out.persistence.mapper;

import com.compass.digitalbank.adapter.out.persistence.entity.AccountEntity;
import com.compass.digitalbank.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceMapper {

    public AccountEntity toNewEntity(Account account) {
        return new AccountEntity(account.getOwnerId(), account.getName(), account.getBalance(), account.isActive());
    }

    public Account toDomain(AccountEntity entity) {
        return Account.restore(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getBalance(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
