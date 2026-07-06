package com.compass.digitalbank.adapter.out.persistence;

import com.compass.digitalbank.domain.pagination.PageResult;
import org.springframework.data.domain.Page;

import java.util.function.Function;

final class PersistencePageMapper {

    private PersistencePageMapper() {
    }

    static <E, D> PageResult<D> toPageResult(Page<E> page, Function<E, D> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
