# SaludYa API — Documentación de Web Services (OpenAPI)

Documentación de los servicios REST del backend de **SaludYa**, generada con
[springdoc-openapi](https://springdoc.org/) (OpenAPI 3) y servida a través de **Swagger UI**.

- **Título / versión del documento:** SaludYa API · v1.0.0
- **Swagger UI (desplegado):** http://3.129.217.49:8080/swagger-ui/index.html
- **Especificación OpenAPI (JSON):** http://3.129.217.49:8080/v3/api-docs
- **Especificación exportada (este repo):** [`docs/api/openapi.json`](./openapi.json)
- **Autenticación:** HTTP Bearer JWT (`bearerAuth`); los endpoints públicos están marcados con `@SecurityRequirements`.

## Cómo usarla

1. Abrir **Swagger UI**.
2. Para endpoints protegidos, pulsar **Authorize** e ingresar el JWT obtenido en
   `POST /api/v1/user-accounts/login/verify` (formato `Bearer <token>`; Swagger agrega el prefijo).
3. La especificación también puede importarse a Postman/Insomnia desde el JSON exportado.

## Inventario de endpoints

### IAM — User accounts (`/api/v1/user-accounts`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/user-accounts/send-verification-code` | Envía un código de verificación por correo antes del registro |
| POST | `/api/v1/user-accounts` | Registra al paciente verificado usando el código recibido |
| POST | `/api/v1/user-accounts/login` | Inicia sesión: valida credenciales y envía un código por correo |
| POST | `/api/v1/user-accounts/login/verify` | Completa el inicio de sesión con el código de correo |
| POST | `/api/v1/user-accounts/login/resend` | Reenvía el código del desafío de inicio de sesión |
| POST | `/api/v1/user-accounts/logout` | Revoca la sesión Bearer actual |
| POST | `/api/v1/user-accounts/recover-password` | Solicita un enlace de recuperación de un solo uso |
| POST | `/api/v1/user-accounts/reset-password` | Canjea el enlace de recuperación y revoca sesiones previas |
| POST | `/api/v1/user-accounts/change-password` | Cambia la contraseña usando la contraseña actual |
| GET | `/api/v1/user-accounts/me` | Lee la cuenta y el perfil de identidad propios |
| GET | `/api/v1/user-accounts/{id}` | Lee el perfil propio (o de otra cuenta si es SUPER_ADMIN) |
| PUT | `/api/v1/user-accounts/{id}` | Actualiza solo correo y celular (identidad/rol inmutables) |
| POST | `/api/v1/user-accounts/staff` | Crea cuenta de personal de admisión e invita a definir contraseña (SUPER_ADMIN) |

### IAM — Identity verification (`/api/v1/identity-verifications`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/identity-verifications` | Verifica que el DNI exista y coincida el nombre completo |
| POST | `/api/v1/identity-verifications/exists` | Comprueba si un DNI es conocido por el proveedor de identidad |

### IAM — Patients (`/api/v1/patients`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/patients/{id}` | Lee el perfil de paciente propio o de un menor vinculado |
| PUT | `/api/v1/patients/{id}` | Actualiza la información de contacto del paciente propio |
| GET | `/api/v1/patients/{id}/minors` | Lista los menores vinculados al paciente tutor |

### IAM — Linked minors (`/api/v1/patient-minors`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/patient-minors` | Vincula a un menor verificado tras confirmar la tutoría |
| GET | `/api/v1/patient-minors/{id}` | Lee el vínculo de tutoría |
| DELETE | `/api/v1/patient-minors/{id}` | Elimina el vínculo de tutoría conservando la historia del menor |

### IAM — Assisted recovery (`/api/v1/account-recovery-requests`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/account-recovery-requests/support` | Devuelve las instrucciones y teléfono de soporte |
| POST | `/api/v1/account-recovery-requests` | Solicita recuperación asistida (no otorga acceso) |
| GET | `/api/v1/account-recovery-requests` | Lista las 100 solicitudes abiertas más antiguas (SUPER_ADMIN) |
| POST | `/api/v1/account-recovery-requests/{id}/resolve` | Resuelve la solicitud tras verificar el DNI físico (SUPER_ADMIN) |

### Appointments & Booking — Appointments (`/api/v1/appointments`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/appointments` | Reserva una cita |
| GET | `/api/v1/appointments` | Lista citas filtradas por paciente, slot, doctor, especialidad, fecha y estado |
| GET | `/api/v1/appointments/{id}` | Obtiene una cita por id |
| GET | `/api/v1/appointments/patient/{patientId}` | Lista las citas de un paciente |
| DELETE | `/api/v1/appointments/{id}` | Cancela una cita |

### Appointments & Booking — Doctors (`/api/v1/doctors`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/doctors` | Lista doctores por especialidad |
| GET | `/api/v1/doctors/{id}` | Obtiene un doctor por id |

### Appointments & Booking — Specialties (`/api/v1/specialties`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/specialties` | Lista todas las especialidades |
| GET | `/api/v1/specialties/{id}` | Obtiene una especialidad por id |

### Appointments & Booking — Time Slots (`/api/v1/time-slots`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/time-slots/available` | Slots disponibles por especialidad y fecha |
| GET | `/api/v1/time-slots` | Slots de un doctor en una fecha |
| GET | `/api/v1/time-slots/{id}` | Obtiene un slot por id |
| POST | `/api/v1/time-slots` | Crea un slot de atención |
| PUT | `/api/v1/time-slots/{id}/capacity` | Actualiza la capacidad de un slot |
| PUT | `/api/v1/time-slots/{id}` | Edita un slot (doctor, horario y estado) |

### Arrival & QR Check-in — Check-ins (`/api/v1/check-ins`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/check-ins/qr` | Registra un check-in a partir de un token QR escaneado |
| POST | `/api/v1/check-ins/code` | Registra un check-in a partir del código de reserva (manual) |
| GET | `/api/v1/check-ins/{id}` | Obtiene el ticket digital de un check-in |
| GET | `/api/v1/check-ins/appointment/{appointmentId}` | Obtiene el ticket digital de una cita |
| GET | `/api/v1/check-ins/appointment/{appointmentId}/qr-token` | Genera el token QR firmado de una cita |
| GET | `/api/v1/check-ins/appointment/{appointmentId}/position` | Posición del paciente en la cola |

### Arrival & QR Check-in — Queue Entries (`/api/v1/queue-entries`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/queue-entries/{id}` | Obtiene una entrada de cola por id |
| POST | `/api/v1/queue-entries/{id}/absent` | Marca una entrada como ausente |
| POST | `/api/v1/queue-entries/{id}/leave` | Sale voluntariamente de la cola (marca la cita ausente) |
| POST | `/api/v1/queue-entries/{id}/start` | Inicia la atención de una entrada llamada |
| POST | `/api/v1/queue-entries/{id}/finish` | Finaliza la atención (marca atendido) |

### Arrival & QR Check-in — Attendance Queues (`/api/v1/attendance-queues`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/attendance-queues` | Resuelve la cola de atención de un slot en una fecha |
| GET | `/api/v1/attendance-queues/{id}/entries` | Lista las entradas de una cola |
| GET | `/api/v1/attendance-queues/{id}/position` | Posición actual en la cola |
| POST | `/api/v1/attendance-queues/{id}/call-next` | Llama al siguiente paciente en espera |

### Reassignment (`/api/v1/reassignment-offers`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/reassignment-offers/pending` | Ofertas de cupo pendientes del usuario actual |
| POST | `/api/v1/reassignment-offers/{id}/accept` | Acepta una oferta de reasignación |
| POST | `/api/v1/reassignment-offers/{id}/reject` | Rechaza una oferta de reasignación |

### Hospital Operations & Configuration (`/api/v1/config`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/config` | Lee la configuración del establecimiento |
| PUT | `/api/v1/config` | Actualiza la configuración del establecimiento |
| GET | `/api/v1/config/dashboard` | Panel con métricas del día |
| GET | `/api/v1/config/reports` | Genera un reporte por rango de fechas y formato |
