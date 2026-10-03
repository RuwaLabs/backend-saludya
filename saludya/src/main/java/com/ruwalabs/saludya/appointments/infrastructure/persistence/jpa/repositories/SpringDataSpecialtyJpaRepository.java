package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.SpecialtyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link SpecialtyJpaEntity}.
 */
@Repository
public interface SpringDataSpecialtyJpaRepository extends JpaRepository<SpecialtyJpaEntity, Long> {
}
