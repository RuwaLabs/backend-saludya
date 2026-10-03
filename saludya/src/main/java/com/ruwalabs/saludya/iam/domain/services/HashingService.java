package com.ruwalabs.saludya.iam.domain.services;

import com.ruwalabs.saludya.iam.domain.model.valueobjects.PasswordHash;
public interface HashingService {
    PasswordHash hash(String password);
    boolean matches(String password, PasswordHash hash);
}
