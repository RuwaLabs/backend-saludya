-- Email ownership verification for accounts created through public registration.
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Accounts created before this change are grandfathered as verified so they keep working.
UPDATE users SET email_verified = TRUE;
