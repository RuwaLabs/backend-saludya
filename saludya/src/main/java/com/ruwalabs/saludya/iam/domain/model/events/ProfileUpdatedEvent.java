package com.ruwalabs.saludya.iam.domain.model.events;

public record ProfileUpdatedEvent(Long userId, String previousEmail) {}
