package com.compass.digitalbank.domain.port.out;

import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByIdForUpdate(UUID id);

    PageResult<Account> findByOwnerId(UUID ownerId, PageQuery pageQuery);
}
