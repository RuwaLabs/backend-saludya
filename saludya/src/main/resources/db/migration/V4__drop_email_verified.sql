-- Email is verified before registration, so the account flag is no longer needed.
ALTER TABLE users DROP COLUMN IF EXISTS email_verified;
