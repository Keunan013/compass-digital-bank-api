package com.compass.digitalbank.adapter.out.persistence;

import com.compass.digitalbank.adapter.out.persistence.mapper.TransferPersistenceMapper;
import com.compass.digitalbank.adapter.out.persistence.repository.TransferJpaRepository;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {

    private static final String CREATED_AT = "createdAt";

    private final TransferJpaRepository jpaRepository;
    private final TransferPersistenceMapper mapper;

    @Override
    public Transfer save(Transfer transfer) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewEntity(transfer)));
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public PageResult<Transfer> findByAccountId(UUID accountId, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, CREATED_AT));
        return PersistencePageMapper.toPageResult(jpaRepository.findByAccountId(accountId, pageable), mapper::toDomain);
    }
}
