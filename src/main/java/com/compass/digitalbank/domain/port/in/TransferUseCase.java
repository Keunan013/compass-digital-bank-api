package com.compass.digitalbank.domain.port.in;

import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferUseCase {

    Transfer transfer(TransferCommand command);

    PageResult<Transfer> listAccountTransactions(UUID accountId, Requester requester, PageQuery pageQuery);

    record TransferCommand(
            Requester requester,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String idempotencyKey
    ) {
    }
}
