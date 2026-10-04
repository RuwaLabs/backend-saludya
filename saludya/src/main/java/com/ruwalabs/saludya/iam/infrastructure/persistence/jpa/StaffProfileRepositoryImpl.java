package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.repositories.StaffProfileRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.StaffProfileEntity;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.StaffProfileJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository @Transactional(readOnly=true)
public class StaffProfileRepositoryImpl implements StaffProfileRepository {
    private final StaffProfileJpaRepository staff;
    public StaffProfileRepositoryImpl(StaffProfileJpaRepository staff) { this.staff=staff; }
    private StaffProfile domain(StaffProfileEntity e) {
        return new StaffProfile(e.getId(),e.getUserId(),new Dni(e.getDni()),e.getName(),e.getLastname(),e.getBirthDate(),e.getPhone());
    }
    @Override @Transactional public StaffProfile save(StaffProfile p) {
        var e=p.id()==null?new StaffProfileEntity():staff.findById(p.id()).orElseThrow();e.setId(p.id());e.setUserId(p.userId());e.setDni(p.dni().value());
        e.setName(p.name());e.setLastname(p.lastname());e.setBirthDate(p.birthDate());e.setPhone(p.phone());return domain(staff.saveAndFlush(e));
    }
    @Override public Optional<StaffProfile> findByUserId(Long id) { return staff.findByUserId(id).map(this::domain); }
    @Override public Optional<StaffProfile> findByDni(String dni) { return staff.findByDni(dni).map(this::domain); }
}
