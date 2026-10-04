package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.repositories.IdentityClaimRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.IdentityClaimEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
@Repository
public class IdentityClaimRepositoryImpl implements IdentityClaimRepository {
    private final EntityManager em;
    public IdentityClaimRepositoryImpl(EntityManager em) { this.em=em; }
    public void claim(String dni,Long userId) {
        var claim=new IdentityClaimEntity();claim.setDni(dni);claim.setUserId(userId);
        em.persist(claim);em.flush(); // INSERT, never merge or replace another owner's claim.
    }
}
