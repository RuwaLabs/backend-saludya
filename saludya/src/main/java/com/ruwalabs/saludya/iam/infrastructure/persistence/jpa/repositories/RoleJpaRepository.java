package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoleJpaRepository extends JpaRepository<RoleEntity,Long> { Optional<RoleEntity> findByName(String name); }
