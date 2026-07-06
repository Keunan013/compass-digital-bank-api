package com.compass.digitalbank.adapter.out.persistence;

import com.compass.digitalbank.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.compass.digitalbank.adapter.out.persistence.repository.UserJpaRepository;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
