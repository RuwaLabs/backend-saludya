package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.seed;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.enums.BookingOrderScope;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.repositories.HospitalConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

/**
 * Seeds the default hospital configuration on startup when none exists.
 */
@Component
@Slf4j
public class HospitalConfigurationSeeder {

    private final HospitalConfigurationRepository repository;

    public HospitalConfigurationSeeder(HospitalConfigurationRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultConfiguration() {
        if (repository.findDefault().isPresent()) {
            return;
        }
        var configuration = HospitalConfiguration.create(
                1,
                BookingOrderScope.PER_SPECIALTY,
                15,
                5,
                10,
                LocalTime.of(20, 0),
                24,
                true);
        repository.save(configuration);
        log.info("Seeded default hospital configuration");
    }
}
