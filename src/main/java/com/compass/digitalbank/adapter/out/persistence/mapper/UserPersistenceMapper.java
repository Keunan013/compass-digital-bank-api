package com.compass.digitalbank.adapter.out.persistence.mapper;

import com.compass.digitalbank.adapter.out.persistence.entity.UserEntity;
import com.compass.digitalbank.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserEntity toNewEntity(User user) {
        return new UserEntity(user.getName(), user.getEmail(), user.getPasswordHash(), user.getRole());
    }

    public User toDomain(UserEntity entity) {
        return User.restore(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.getCreatedAt());
    }
}
