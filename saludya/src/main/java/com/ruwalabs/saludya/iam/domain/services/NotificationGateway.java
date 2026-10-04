package com.ruwalabs.saludya.iam.domain.services;

/** Queues messages transactionally; delivery happens after commit. */
public interface NotificationGateway {
    void enqueue(String recipient, String subject, String body);
}
