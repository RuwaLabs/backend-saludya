# backend-saludya

Backend API builded with Spring Boot

---

## Database Configuration

**Database:** PostgreSQL

---

## Database Creation Script

```sql
-- ============================================================
-- SALUDYA - SISTEMA DE DOBLE COLA
-- POSTGRESQL DATABASE CREATION SCRIPT
-- ============================================================

-- ============================================================
-- BOUNDED CONTEXT: Identity & Access Management
-- ============================================================

CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    id_role     INT NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (id_role) REFERENCES roles(id)
);

CREATE TABLE patients (
    id          SERIAL PRIMARY KEY,
    id_user     INT NOT NULL UNIQUE,
    dni         VARCHAR(8) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    lastname    VARCHAR(100) NOT NULL,
    birth_date  DATE NULL,
    phone       VARCHAR(20) NULL,
    CONSTRAINT fk_patients_user FOREIGN KEY (id_user) REFERENCES users(id)
);

CREATE TABLE patient_minors (
    id          SERIAL PRIMARY KEY,
    id_patient  INT NOT NULL UNIQUE,
    id_tutor    INT NOT NULL,
    CONSTRAINT fk_patient_minors_patient FOREIGN KEY (id_patient) REFERENCES patients(id),
    CONSTRAINT fk_patient_minors_tutor FOREIGN KEY (id_tutor) REFERENCES patients(id)
);

-- ============================================================
-- BOUNDED CONTEXT: Hospital Operations & Configuration
-- ============================================================

CREATE TABLE hospital_configurations (
    id                                SERIAL PRIMARY KEY,
    max_capacity_per_slot             INT NOT NULL,
    booking_order_scope               VARCHAR(20) NOT NULL DEFAULT 'PER_SPECIALTY',
    check_in_tolerance_minutes        INT NOT NULL DEFAULT 15,
    post_call_tolerance_minutes       INT NOT NULL DEFAULT 5,
    reassignment_response_timeout_min INT NOT NULL DEFAULT 10,
    booking_cutoff_time               TIME NOT NULL,
    cancellation_deadline_hours       INT NOT NULL DEFAULT 24,
    attendance_queue_visible          BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at                        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_order_scope CHECK (booking_order_scope IN ('GLOBAL', 'PER_SPECIALTY')),
    CONSTRAINT chk_max_capacity CHECK (max_capacity_per_slot > 0),
    CONSTRAINT chk_check_in_tolerance CHECK (check_in_tolerance_minutes >= 0),
    CONSTRAINT chk_post_call_tolerance CHECK (post_call_tolerance_minutes >= 0),
    CONSTRAINT chk_reassignment_timeout CHECK (reassignment_response_timeout_min >= 0)
);

-- ============================================================
-- BOUNDED CONTEXT: Appointments & Booking
-- ============================================================

CREATE TABLE specialties (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL
);

CREATE TABLE doctors (
    id            SERIAL PRIMARY KEY,
    id_specialty  INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    lastname      VARCHAR(100) NOT NULL,
    CONSTRAINT fk_doctors_specialty FOREIGN KEY (id_specialty) REFERENCES specialties(id)
);

CREATE TABLE time_slots (
    id               SERIAL PRIMARY KEY,
    id_doctor        INT NOT NULL,
    date             DATE NOT NULL,
    start_hour       TIME NOT NULL,
    end_hour         TIME NOT NULL,
    max_capacity     INT NOT NULL,
    current_bookings INT NOT NULL DEFAULT 0,
    status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_time_slots_doctor_date_hour UNIQUE (id_doctor, date, start_hour),
    CONSTRAINT fk_time_slots_doctor FOREIGN KEY (id_doctor) REFERENCES doctors(id),
    CONSTRAINT chk_time_slots_status CHECK (status IN ('AVAILABLE', 'FULL', 'CANCELLED')),
    CONSTRAINT chk_time_slots_max_capacity CHECK (max_capacity > 0),
    CONSTRAINT chk_time_slots_current_bookings CHECK (current_bookings >= 0 AND current_bookings <= max_capacity)
);

CREATE TABLE appointments (
    id              SERIAL PRIMARY KEY,
    id_time_slot    INT NOT NULL,
    id_patient      INT NOT NULL,
    booking_order   INT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    CONSTRAINT fk_appointments_time_slot FOREIGN KEY (id_time_slot) REFERENCES time_slots(id),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (id_patient) REFERENCES patients(id),
    CONSTRAINT chk_appointments_status CHECK (status IN ('RESERVED', 'CONFIRMED', 'CANCELLED', 'ABSENT', 'ATTENDED', 'EXPIRED'))
);

CREATE INDEX idx_appointments_booking_order ON appointments (booking_order);
CREATE INDEX idx_appointments_time_slot ON appointments (id_time_slot);
CREATE INDEX idx_appointments_patient ON appointments (id_patient);

-- ============================================================
-- BOUNDED CONTEXT: Reassignment
-- ============================================================

CREATE TABLE reassignment_offers (
    id                      SERIAL PRIMARY KEY,
    id_appointment          INT NOT NULL,
    id_freed_time_slot      INT NOT NULL,
    id_original_appointment INT NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    offered_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at            TIMESTAMP NULL,
    expires_at              TIMESTAMP NOT NULL,
    CONSTRAINT fk_reassignment_appointment FOREIGN KEY (id_appointment) REFERENCES appointments(id),
    CONSTRAINT fk_reassignment_freed_time_slot FOREIGN KEY (id_freed_time_slot) REFERENCES time_slots(id),
    CONSTRAINT fk_reassignment_original_appointment FOREIGN KEY (id_original_appointment) REFERENCES appointments(id),
    CONSTRAINT chk_reassignment_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'))
);

CREATE INDEX idx_reassignment_offers_status ON reassignment_offers (status);
CREATE INDEX idx_reassignment_offers_appointment ON reassignment_offers (id_appointment);

-- ============================================================
-- BOUNDED CONTEXT: Arrival & QR Check-in
-- ============================================================

CREATE TABLE check_ins (
    id              SERIAL PRIMARY KEY,
    id_appointment  INT NOT NULL UNIQUE,
    qr_token        VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL DEFAULT 'VALID',
    checked_in_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_check_ins_appointment FOREIGN KEY (id_appointment) REFERENCES appointments(id),
    CONSTRAINT chk_check_ins_status CHECK (status IN ('VALID', 'EXPIRED', 'INVALID'))
);

CREATE TABLE attendance_queues (
    id              SERIAL PRIMARY KEY,
    id_time_slot    INT NOT NULL,
    date            DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    CONSTRAINT uq_attendance_queues_time_slot_date UNIQUE (id_time_slot, date),
    CONSTRAINT fk_attendance_queues_time_slot FOREIGN KEY (id_time_slot) REFERENCES time_slots(id),
    CONSTRAINT chk_attendance_queues_status CHECK (status IN ('OPEN', 'CLOSED', 'PAUSED'))
);

CREATE TABLE queue_entries (
    id                  SERIAL PRIMARY KEY,
    id_attendance_queue INT NOT NULL,
    id_check_in         INT NOT NULL UNIQUE,
    position            INT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    called_at           TIMESTAMP NULL,
    attended_at         TIMESTAMP NULL,
    CONSTRAINT fk_queue_entries_attendance_queue FOREIGN KEY (id_attendance_queue) REFERENCES attendance_queues(id),
    CONSTRAINT fk_queue_entries_check_in FOREIGN KEY (id_check_in) REFERENCES check_ins(id),
    CONSTRAINT chk_queue_entries_status CHECK (status IN ('WAITING', 'CALLED', 'IN_ATTENTION', 'ATTENDED', 'ABSENT'))
);

CREATE INDEX idx_queue_entries_queue ON queue_entries (id_attendance_queue, position);
CREATE INDEX idx_queue_entries_status ON queue_entries (status);

-- ============================================================
-- END OF SCRIPT
-- ============================================================
