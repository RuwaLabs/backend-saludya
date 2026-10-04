package com.ruwalabs.saludya.iam.domain.model.entities;

import java.time.Instant;
import java.util.UUID;
public record RecoveryHelpRequest(UUID id,String dni,String contactEmail,Instant createdAt,String status,
        Long resolvedBy,Instant resolvedAt) { }
