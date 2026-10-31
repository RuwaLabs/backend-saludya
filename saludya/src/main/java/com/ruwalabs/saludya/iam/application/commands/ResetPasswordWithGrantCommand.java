package com.ruwalabs.saludya.iam.application.commands;

public record ResetPasswordWithGrantCommand(String resetToken, String password, String confirmPassword) { }
