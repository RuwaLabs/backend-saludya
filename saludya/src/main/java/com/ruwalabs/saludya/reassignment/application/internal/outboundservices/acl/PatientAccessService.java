package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

/**
 * ACL port used to authorize operations on a patient profile.
 *
 * <p>Staff and administrators are always allowed; patients may only act on their
 * own profile or a minor they tutor.</p>
 */
public interface PatientAccessService {

    /**
     * Checks whether the authenticated caller manages the given patient profile.
     *
     * @param patientId the patient profile identifier
     * @return {@code true} if the caller may act on behalf of the patient
     */
    boolean canManagePatient(Long patientId);
}
