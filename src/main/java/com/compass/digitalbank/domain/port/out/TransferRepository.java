package com.compass.digitalbank.domain.port.out;

import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    PageResult<Transfer> findByAccountId(UUID accountId, PageQuery pageQuery);
}
