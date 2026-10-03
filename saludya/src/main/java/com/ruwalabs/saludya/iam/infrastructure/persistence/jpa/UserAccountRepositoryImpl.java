package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.repositories.UserAccountRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.*;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.mappers.UserAccountPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository @Transactional(readOnly=true)
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountJpaRepository users; private final RoleJpaRepository roles;
    public UserAccountRepositoryImpl(UserAccountJpaRepository users,RoleJpaRepository roles) { this.users=users; this.roles=roles; }
    @Override @Transactional public UserAccount save(UserAccount a) {
        var role=roles.findByName(a.getRole().name()).orElseThrow();
        var e=a.getId()==null?UserAccountPersistenceMapper.toEntity(a,role):users.findById(a.getId()).orElseThrow();
        e.setEmail(a.getEmail().value());e.setPassword(a.getPassword().value());e.setRole(role);e.setActive(a.isActive());
        return UserAccountPersistenceMapper.toDomain(users.saveAndFlush(e));
    }
    @Override public Optional<UserAccount> findById(Long id) { return users.findById(id).map(UserAccountPersistenceMapper::toDomain); }
    @Override public Optional<UserAccount> findByEmail(String email) { return users.findByEmail(email).map(UserAccountPersistenceMapper::toDomain); }
    @Override public Optional<UserAccount> lockById(Long id) { return users.lockById(id).map(UserAccountPersistenceMapper::toDomain); }
}
