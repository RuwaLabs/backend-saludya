package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.entities.RecoveryHelpRequest;
import com.ruwalabs.saludya.iam.domain.repositories.RecoveryHelpRequestRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.RecoveryHelpRequestJpaRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.RecoveryHelpRequestEntity;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public class RecoveryHelpRequestRepositoryImpl implements RecoveryHelpRequestRepository {
    private final RecoveryHelpRequestJpaRepository requests;
    public RecoveryHelpRequestRepositoryImpl(RecoveryHelpRequestJpaRepository requests) { this.requests=requests; }
    private RecoveryHelpRequest domain(RecoveryHelpRequestEntity e) {
        return new RecoveryHelpRequest(e.getId(),e.getDni(),e.getContactEmail(),e.getCreatedAt(),e.getStatus(),e.getResolvedBy(),e.getResolvedAt());
    }
    public RecoveryHelpRequest save(RecoveryHelpRequest r) {
        var e=new RecoveryHelpRequestEntity();e.setId(r.id());e.setDni(r.dni());e.setContactEmail(r.contactEmail());
        e.setCreatedAt(r.createdAt());e.setStatus(r.status());e.setResolvedBy(r.resolvedBy());e.setResolvedAt(r.resolvedAt());
        return domain(requests.saveAndFlush(e));
    }
    public Optional<RecoveryHelpRequest> lockById(UUID id) { return requests.lockById(id).map(this::domain); }
    public List<RecoveryHelpRequest> findOpen() { return requests.findTop100ByStatusOrderByCreatedAtAsc("OPEN").stream().map(this::domain).toList(); }
}
