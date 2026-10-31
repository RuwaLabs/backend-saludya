# Admission staff seed

The application includes an opt-in, idempotent bootstrap for pre-created `ADMISSION_STAFF` accounts. It is disabled by default and does not expose an HTTP registration route.

## Configure a deployment

In the backend deployment environment, set:

- `IAM_STAFF_SEED_ENABLED=true`
- `IAM_STAFF_SEED_ACCOUNTS` to a JSON array. Each entry must include `email`, `dni`, `name`, `lastname`, `birthDate` (`YYYY-MM-DD`), `phone`, and `password`.

Example format (use private values; do not commit them):

```json
[{"email":"staff@example.test","dni":"00000000","name":"Staff","lastname":"Example","birthDate":"1990-01-01","phone":"900000000","password":"ReplaceWithSecurePassword1"}]
```

Use the deployment provider's secret/environment-variable UI; do not place actual account data or passwords in source files. Passwords must satisfy the existing policy (at least 8 characters, one letter, one number, maximum 72 UTF-8 bytes). The seed hashes them using the backend's BCrypt service.

Deploy/restart the backend with these variables. After startup completes and the accounts are provisioned, set `IAM_STAFF_SEED_ENABLED=false` and remove `IAM_STAFF_SEED_ACCOUNTS` from the environment. The operation is idempotent: repeated runs do not duplicate accounts, reset passwords, or overwrite profiles. An email/role or email/DNI conflict fails startup rather than silently changing an account.

Login remains email + password followed by the existing emailed verification code. Configure the backend's existing mail delivery variables so the staff can receive their codes and password-recovery emails.

The Flutter app's password-recovery flow uses `POST /user-accounts/recover-password/code`, `/recover-password/code/verify`, and `/recover-password/code/reset`: the code expires after 15 minutes, has five attempts, and is exchanged for a short-lived one-use reset grant before the password can be changed. The original link-based `recover-password` and `reset-password` endpoints remain available for existing clients. Those legacy links use `IAM_PASSWORD_RESET_URL` (HTTPS); its fallback is intentionally non-routable.
