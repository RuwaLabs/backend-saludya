package com.ruwalabs.saludya.iam.application.internal.queryservices;

import com.ruwalabs.saludya.iam.application.queryservices.UserAccountQueryService;
import com.ruwalabs.saludya.iam.application.results.AccountProfile;
import com.ruwalabs.saludya.iam.application.internal.AccessPolicy;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class UserAccountQueryServiceImpl implements UserAccountQueryService {
    private final UserAccountRepository users;private final PatientRepository patients;
    private final StaffProfileRepository staff;private final AccessPolicy access;
    public UserAccountQueryServiceImpl(UserAccountRepository users,PatientRepository patients,StaffProfileRepository staff,AccessPolicy access) {
        this.users=users;this.patients=patients;this.staff=staff;this.access=access;
    }
    public AccountProfile getById(Long actorId,Long userId) {
        var actor=access.actor(actorId);
        if (!actorId.equals(userId)&&actor.getRole()!=Role.SUPER_ADMIN) throw IamException.forbidden();
        return profile(userId);
    }
    public AccountProfile profile(Long userId) {
        var u=users.findById(userId).orElseThrow(IamException::notFound);
        if (u.getRole()==Role.PATIENT) {
            var p=patients.findByUserId(userId).orElseThrow(IamException::notFound);
            return new AccountProfile(u.getId(),u.getRole(),u.getEmail().value(),u.isActive(),p.getId(),
                    p.getDni().value(),p.getName(),p.getLastname(),p.getBirthDate(),p.getPhone());
        }
        var s=staff.findByUserId(userId).orElseThrow(IamException::notFound);
        return new AccountProfile(u.getId(),u.getRole(),u.getEmail().value(),u.isActive(),null,
                s.dni().value(),s.name(),s.lastname(),s.birthDate(),s.phone());
    }
}
