package com.ruwalabs.saludya.reassignment.infrastructure.scheduler;

import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for the {@code Reassignment} bounded context.
 *
 * <p>Periodically closes offers whose single window ({@code expiresAt}) has passed:</p>
 * <ul>
 *   <li>{@code PENDING} offers past their window → {@code EXPIRED} (candidate never responded).</li>
 *   <li>{@code ACCEPTED} offers past their window → {@code ABSENT} (candidate never arrived).</li>
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

    public ReassignmentExpirationScheduler(ReassignmentOfferRepository reassignmentOfferRepository) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
    }

    /**
     * Expires pending offers and marks accepted-but-absent offers every 60 seconds.
     */
    @Scheduled(fixedDelay = 60_000)
    public void processOverdueOffers() {
        expirePendingOffers();
        markAcceptedNoShows();
    }

    private void expirePendingOffers() {
        for (var offer : reassignmentOfferRepository.findExpiredOffers()) {
            offer.expire();
            reassignmentOfferRepository.save(offer);
            log.info("Reassignment offer {} expired without response", offer.getId());
        }
    }

    private void markAcceptedNoShows() {
        for (var offer : reassignmentOfferRepository.findAcceptedOverdue()) {
            offer.markNoShow();
            reassignmentOfferRepository.save(offer);
            log.info("Reassignment offer {} declared as no-show", offer.getId());
        }
    }
}
