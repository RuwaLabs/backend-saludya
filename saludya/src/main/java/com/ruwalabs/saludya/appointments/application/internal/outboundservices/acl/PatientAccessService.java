package com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl;

/**
 * ACL port used to authorize operations on a patient profile.
 *
 * <p>It answers whether the authenticated caller may act on behalf of the given
 * {@code patientId}: their own profile or a minor they tutor. Staff and
 * administrators are always allowed.</p>
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
