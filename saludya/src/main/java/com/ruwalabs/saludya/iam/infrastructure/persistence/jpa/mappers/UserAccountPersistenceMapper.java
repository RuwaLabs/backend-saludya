package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.*;
public final class UserAccountPersistenceMapper {
    private UserAccountPersistenceMapper() {}
    public static UserAccount toDomain(UserAccountEntity e) {
        return new UserAccount(e.getId(),new Email(e.getEmail()),new PasswordHash(e.getPassword()),
                Role.valueOf(e.getRole().getName()),e.isActive(),e.getCreatedAt().toInstant());
    }
    public static UserAccountEntity toEntity(UserAccount account, RoleEntity role) {
        var e=new UserAccountEntity(); e.setId(account.getId()); e.setEmail(account.getEmail().value());
        e.setPassword(account.getPassword().value()); e.setRole(role); e.setActive(account.isActive()); return e;
    }
}
