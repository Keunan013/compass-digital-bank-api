package com.compass.digitalbank.adapter.out.persistence.repository;

import com.compass.digitalbank.adapter.out.persistence.entity.TransferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, UUID> {

    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select t from TransferEntity t
            where t.sourceAccountId = :accountId or t.destinationAccountId = :accountId
            """)
    Page<TransferEntity> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
}
