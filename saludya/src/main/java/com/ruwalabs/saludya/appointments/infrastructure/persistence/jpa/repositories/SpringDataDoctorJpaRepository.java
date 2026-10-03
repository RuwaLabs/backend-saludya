package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.DoctorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link DoctorJpaEntity}.
 */
@Repository
public interface SpringDataDoctorJpaRepository extends JpaRepository<DoctorJpaEntity, Long> {

    List<DoctorJpaEntity> findBySpecialtyId(Long specialtyId);
}
