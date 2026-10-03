package com.ruwalabs.saludya.iam.domain.model.aggregates;

import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.util.Objects;
@Getter
public class UserAccount extends AbstractDomainAggregateRoot<UserAccount> {
    private final Long id;
    private Email email;
    private PasswordHash password;
    private final Role role;
    private boolean active;
    private final Instant createdAt;
    public UserAccount(Long id, Email email, PasswordHash password, Role role, boolean active, Instant createdAt) {
        this.id = id; this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password); this.role = Objects.requireNonNull(role);
        this.active = active; this.createdAt = Objects.requireNonNull(createdAt);
    }
    public void updateEmail(Email email) { this.email = Objects.requireNonNull(email); }
    public void changePassword(PasswordHash password) { this.password = Objects.requireNonNull(password); }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
}
