package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import java.util.Optional;
public interface UserAccountRepository {
    UserAccount save(UserAccount account);
    Optional<UserAccount> findById(Long id);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> lockById(Long id);
}
