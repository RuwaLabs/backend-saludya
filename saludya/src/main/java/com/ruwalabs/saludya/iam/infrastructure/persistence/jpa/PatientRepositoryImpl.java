package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.domain.repositories.PatientRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.PatientJpaRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.mappers.PatientPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository @Transactional(readOnly=true)
public class PatientRepositoryImpl implements PatientRepository {
    private final PatientJpaRepository patients;
    public PatientRepositoryImpl(PatientJpaRepository patients) { this.patients=patients; }
    @Override @Transactional public Patient save(Patient p) { var e=p.getId()==null?PatientPersistenceMapper.toEntity(p):patients.findById(p.getId()).orElseThrow();
        e.setPhone(p.getPhone());return PatientPersistenceMapper.toDomain(patients.saveAndFlush(e)); }
    @Override public Optional<Patient> findById(Long id) { return patients.findById(id).map(PatientPersistenceMapper::toDomain); }
    @Override public Optional<Patient> findByDni(String dni) { return patients.findByDni(dni).map(PatientPersistenceMapper::toDomain); }
    @Override public Optional<Patient> findByUserId(Long id) { return patients.findByUserId(id).map(PatientPersistenceMapper::toDomain); }
    @Override public Optional<Patient> lockById(Long id) { return patients.lockById(id).map(PatientPersistenceMapper::toDomain); }
}
