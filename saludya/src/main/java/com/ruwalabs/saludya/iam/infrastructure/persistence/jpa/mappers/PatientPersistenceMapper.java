package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PatientEntity;
public final class PatientPersistenceMapper {
    private PatientPersistenceMapper() {}
    public static Patient toDomain(PatientEntity e) {
        return new Patient(e.getId(),e.getUserId(),new Dni(e.getDni()),e.getName(),e.getLastname(),e.getBirthDate(),e.getPhone());
    }
    public static PatientEntity toEntity(Patient p) {
        var e=new PatientEntity(); e.setId(p.getId()); e.setUserId(p.getUserId()); e.setDni(p.getDni().value());
        e.setName(p.getName()); e.setLastname(p.getLastname()); e.setBirthDate(p.getBirthDate()); e.setPhone(p.getPhone()); return e;
    }
}
