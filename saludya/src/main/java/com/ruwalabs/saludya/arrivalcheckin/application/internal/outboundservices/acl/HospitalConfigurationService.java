package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

/**
 * Port for reading operational configuration owned by the
 * {@code Hospital Operations & Configuration} bounded context.
 */
public interface HospitalConfigurationService {

    /**
     * @return the check-in tolerance window in minutes
     */
    int checkInToleranceMinutes();

    /**
     * @return the post-call tolerance window in minutes
     */
    int postCallToleranceMinutes();

    /**
     * @return whether the attendance queue is visible to patients
     */
    boolean attendanceQueueVisible();
}
