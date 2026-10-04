package com.ruwalabs.saludya.iam.infrastructure.events;

import com.ruwalabs.saludya.iam.domain.services.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
@Component("iamEventPublisher")
public class SpringEventPublisherImpl implements EventPublisher {
    private final ApplicationEventPublisher publisher;
    public SpringEventPublisherImpl(ApplicationEventPublisher publisher) { this.publisher=publisher; }
    public void publish(Object event) { publisher.publishEvent(event); }
}
