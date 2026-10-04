package com.ruwalabs.saludya.reassignment.infrastructure.scheduler;

import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for the {@code Reassignment} bounded context.
 *
 * <p>Periodically:</p>
 * <ul>
 *   <li>{@code PENDING} offers past their response window → {@code EXPIRED} (candidate never responded).</li>
 *   <li>{@code ACCEPTED} offers whose candidate did not arrive before the arrival window
 *       (destination slot start + check-in tolerance) → {@code ABSENT} (no-show).</li>
 * </ul>
 *
 * <p>Saving the aggregate publishes the corresponding domain event
 * ({@code ReassignmentOfferExpiredEvent} / {@code ReassignmentOfferNoShowEvent}), which in
 * turn advances the reassignment chain.</p>
 */
@Component
@Slf4j
public class ReassignmentExpirationScheduler {

    private final ReassignmentOfferRepository reassignmentOfferRepository;
    private final ReassignmentCommandService reassignmentCommandService;

    public ReassignmentExpirationScheduler(
            ReassignmentOfferRepository reassignmentOfferRepository,
            ReassignmentCommandService reassignmentCommandService) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
        this.reassignmentCommandService = reassignmentCommandService;
    }

    /**
     * Expires pending offers and detects no-shows every 60 seconds.
     */
    @Scheduled(fixedDelay = 60_000)
    public void processOverdueOffers() {
        expirePendingOffers();
        detectNoShows();
    }

    private void expirePendingOffers() {
        for (var offer : reassignmentOfferRepository.findExpiredOffers()) {
            offer.expire();
            reassignmentOfferRepository.save(offer);
            log.info("Reassignment offer {} expired without response", offer.getId());
        }
    }

    private void detectNoShows() {
        var detected = reassignmentCommandService.detectNoShows();
        if (detected > 0) {
            log.info("Reassignment marked {} accepted offers as no-show", detected);
        }
    }
}
