package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

/**
 * Outbound port for arrival notifications.
 */
public interface NotificationService {

    /**
     * Notifies the patient that the check-in was completed and their queue position.
     *
     * @param appointmentId the appointment identifier
     * @param position      the assigned position
     */
    void notifyCheckInCompleted(Long appointmentId, int position);

    /**
     * Notifies the patient that they are being called.
     *
     * @param queueEntryId the queue entry identifier
     * @param position     the patient's position
     */
    void notifyPatientCalled(Long queueEntryId, int position);
}
