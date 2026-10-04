package com.ruwalabs.saludya.iam.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * IAM infrastructure configuration.
 *
 * <p>Auditing and scheduling are enabled once on {@code SaludyaApplication}; this
 * class only contributes the shared {@link Clock} used by the IAM services.</p>
 */
@Configuration
public class IamConfiguration {

    @Bean
    public Clock iamClock() {
        return Clock.systemUTC();
    }
}
