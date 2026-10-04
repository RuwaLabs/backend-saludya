package com.ruwalabs.saludya.iam.infrastructure.authorization;

import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import java.security.Principal;
import java.util.UUID;
public record IamPrincipal(Long userId,Role role,UUID sessionId) implements Principal {
    @Override public String getName() { return userId.toString(); }
}
