package com.ruwalabs.saludya.appointments.infrastructure.acl;

import com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.iam.interfaces.acl.IamContextFacade;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * ACL adapter that authorizes patient operations using the IAM facade.
 *
 * <p>Reads the authenticated principal from the security context (its name is the
 * account id) and delegates to {@link IamContextFacade#canManagePatient}. Staff and
 * administrators bypass the ownership check.</p>
 */
@Service("appointmentsPatientAccessService")
public class IamPatientAccessService implements PatientAccessService {

    private final IamContextFacade iamContextFacade;

    public IamPatientAccessService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public boolean canManagePatient(Long patientId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        var isStaffOrAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMISSION_STAFF") || authority.equals("ROLE_SUPER_ADMIN"));
        if (isStaffOrAdmin) {
            return true;
        }
        try {
            var accountId = Long.valueOf(authentication.getName());
            return iamContextFacade.canManagePatient(accountId, patientId);
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
