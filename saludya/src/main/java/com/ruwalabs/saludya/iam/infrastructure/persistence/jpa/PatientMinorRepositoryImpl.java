package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import com.ruwalabs.saludya.iam.domain.repositories.PatientMinorRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PatientMinorEntity;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.PatientMinorJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Repository @Transactional(readOnly=true)
public class PatientMinorRepositoryImpl implements PatientMinorRepository {
    private final PatientMinorJpaRepository links;
    public PatientMinorRepositoryImpl(PatientMinorJpaRepository links) { this.links=links; }
    private PatientMinor domain(PatientMinorEntity e) { return new PatientMinor(e.getId(),e.getPatientId(),e.getTutorId()); }
    @Override @Transactional public PatientMinor save(PatientMinor p) {
        var e=new PatientMinorEntity();e.setId(p.id());e.setPatientId(p.patientId());e.setTutorId(p.tutorId());return domain(links.saveAndFlush(e));
    }
    @Override public Optional<PatientMinor> findById(Long id) { return links.findById(id).map(this::domain); }
    @Override public Optional<PatientMinor> findByPatientId(Long id) { return links.findByPatientId(id).map(this::domain); }
    @Override public List<PatientMinor> findMinorsByTutor(Long id) { return links.findByTutorIdOrderById(id).stream().map(this::domain).toList(); }
    @Override @Transactional public void delete(PatientMinor p) { links.deleteById(p.id()); links.flush(); }
}
