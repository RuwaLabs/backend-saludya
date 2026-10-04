package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import java.util.Optional;
public interface StaffProfileRepository {
    StaffProfile save(StaffProfile profile);
    Optional<StaffProfile> findByUserId(Long id);
    Optional<StaffProfile> findByDni(String dni);
}
