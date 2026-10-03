package com.ruwalabs.saludya.iam.infrastructure.configuration;

import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;
@Configuration @EnableJpaAuditing @EnableScheduling
public class IamConfiguration {
    @Bean public Clock iamClock() { return Clock.systemUTC(); }
}
