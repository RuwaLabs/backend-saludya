package com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.CheckInStatus;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * CheckIn aggregate root.
 *
 * <p>Represents the validation of the patient's physical presence. It controls the
 * lifecycle of the check-in (VALID → EXPIRED/INVALID).</p>
 */
@Getter
public class CheckIn extends AbstractDomainAggregateRoot<CheckIn> {

    private final Long id;
    private final Long idAppointment;
    private final String qrToken;
    private CheckInStatus status;
    private final Instant checkedInAt;

    public CheckIn(
            Long id,
            Long idAppointment,
            String qrToken,
            CheckInStatus status,
            Instant checkedInAt) {
        this.id = id;
        this.idAppointment = Objects.requireNonNull(idAppointment, "idAppointment must not be null");
        this.qrToken = qrToken;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.checkedInAt = checkedInAt;
    }

    /**
     * Factory method that creates a valid check-in.
     *
     * @param idAppointment the appointment identifier
     * @param qrToken       the validated QR token
     * @return a new valid check-in
     */
    public static CheckIn create(Long idAppointment, String qrToken) {
        return new CheckIn(null, idAppointment, qrToken, CheckInStatus.VALID, Instant.now());
    }

    /**
     * Marks the check-in as expired.
     */
    public void expire() {
        this.status = CheckInStatus.EXPIRED;
    }

    /**
     * Marks the check-in as invalid.
     */
    public void invalidate() {
        this.status = CheckInStatus.INVALID;
    }

    /**
     * Indicates whether the check-in is valid.
     *
     * @return {@code true} if valid
     */
    public boolean isValid() {
        return this.status == CheckInStatus.VALID;
    }
}
