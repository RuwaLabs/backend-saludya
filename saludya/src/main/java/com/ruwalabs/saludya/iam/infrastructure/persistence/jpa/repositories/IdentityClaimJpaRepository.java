package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.IdentityClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IdentityClaimJpaRepository extends JpaRepository<IdentityClaimEntity,String> { }
