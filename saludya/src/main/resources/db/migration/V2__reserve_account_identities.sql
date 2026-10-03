CREATE TABLE identity_claims (
    dni VARCHAR(8) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id)
);
INSERT INTO identity_claims(dni,user_id) SELECT dni,id_user FROM patients WHERE id_user IS NOT NULL;
INSERT INTO identity_claims(dni,user_id) SELECT dni,id_user FROM staff_profiles;
