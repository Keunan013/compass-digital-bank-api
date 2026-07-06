package com.compass.digitalbank.adapter.out.persistence.mapper;

import com.compass.digitalbank.adapter.out.persistence.entity.TransferEntity;
import com.compass.digitalbank.domain.model.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferPersistenceMapper {

    public TransferEntity toNewEntity(Transfer transfer) {
        return new TransferEntity(
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getIdempotencyKey());
    }

    public Transfer toDomain(TransferEntity entity) {
        return Transfer.restore(
                entity.getId(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt());
    }
}
