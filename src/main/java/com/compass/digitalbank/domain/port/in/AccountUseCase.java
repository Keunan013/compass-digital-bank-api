package com.compass.digitalbank.domain.port.in;

import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountUseCase {

    Account create(CreateAccountCommand command);

    Account getById(UUID accountId, Requester requester);

    PageResult<Account> listOwnedBy(UUID ownerId, PageQuery pageQuery);

    record CreateAccountCommand(UUID ownerId, String name, BigDecimal initialBalance) {
    }
}
