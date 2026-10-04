package com.ruwalabs.saludya.iam.application.commands;

public record UpdateProfileCommand(Long actorId, Long userId, String email, String phone) {}
