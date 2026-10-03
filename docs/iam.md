# Identity & Access Management — SaludYa

Implementación del bounded context IAM en la rama `iam`, basada en las historias US-01 a US-06 y la sección 2.6.1 del reporte de RuwaLabs. Spring Boot 4.1.1, Java 25, PostgreSQL, arquitectura por capas y dominio separado de JPA.

## Alcance y relación con el reporte

| Historia | Comportamiento implementado | Evidencia automatizada |
| --- | --- | --- |
| US-01 — Registro | DNI de 8 dígitos, contraste de nombres/apellidos/fecha de nacimiento con un proveedor confiable, titular de 18 años o más, teléfono, correo único, contraseña BCrypt y correo de bienvenida. Rol público fijo PATIENT. | Registro válido, datos inconsistentes, menor de edad, DNI desconocido, correo/DNI duplicados, registro concurrente y rol falsificado. |
| US-02 — Personal administrativo | Solo SUPER_ADMIN crea ADMISSION_STAFF con identidad verificada, teléfono y correo corporativo permitido. Se envía una invitación para definir la contraseña. | Permisos de paciente/personal, validación de identidad, duplicados, dominio corporativo e invitación utilizable. |
| US-03 — Menores | Perfil del menor sin cuenta de acceso, tutor adulto, validación de identidad y filiación, un tutor por menor, alerta ante vínculo existente y desvinculación que conserva el paciente. | Filiación incorrecta, menor/adulto, consentimiento, duplicados, acceso del tutor y conservación del perfil tras desvincular. |
| US-04 — Recuperación | Enlace de un uso válido 15 minutos, confirmación genérica para cuentas inexistentes, tokens almacenados como SHA-256, revocación de sesiones y solicitud de atención presencial por pérdida total. | Expiración, confirmación, reutilización, solicitud nueva, redención concurrente, cifrado de correos, solicitud y aprobación administrativa auditada. |
| US-05 — Perfil | Cada cuenta actualiza únicamente su correo y teléfono. Identidad y rol inmutables. Avisos al correo nuevo y anterior. | Acceso a perfiles ajenos rechazado, actualización de paciente y personal, preservación de identidad y fecha de creación. |
| US-06 — Inicio de sesión | Correo **o** DNI, contraseña y rol seleccionado; JWT firmado, caducidad, cuenta activa y sesión persistida. Cierre de sesión efectivo. | Credenciales/rol incorrectos, cuenta inexistente o inactiva, JWT alterado, expiración y revocación de una sesión. |

El alta de personal entrega el correo de acceso, el rol y un enlace temporal para establecer la clave. Esta decisión evita enviar contraseñas en texto plano, manteniendo la entrega de acceso prevista en US-02.

Los wireframes muestran verificación por celular. Las historias funcionales de IAM especifican verificación oficial de identidad y recuperación por correo; esta implementación sigue ese contrato. No se simula un servicio SMS ni se afirma que exista autenticación OTP. Si se incorpora SMS como requisito, debe añadirse un proveedor y su contrato antes de conectar esas pantallas a ese flujo.

## Estructura

```text
saludya/src/main/java/com/ruwalabs/saludya/iam/
├── domain/
│   ├── model/aggregates/        UserAccount, Patient
│   ├── model/entities/          PatientMinor, StaffProfile, sesiones y recuperación
│   ├── model/valueobjects/      Email, Dni, PhoneNumber, PasswordHash
│   ├── model/enums/             PATIENT, ADMISSION_STAFF, SUPER_ADMIN
│   ├── model/events/            Registro, perfil y vínculos
│   ├── model/factories/         UserAccountFactory
│   ├── repositories/            Puertos de persistencia
│   └── services/                Puertos de identidad, hashing, tokens y notificaciones
├── application/
│   ├── commands/               Comandos de los casos de uso
│   ├── commandservices/        Contratos de escritura
│   ├── queryservices/          Contratos de consulta
│   ├── results/                DTO sin credenciales
│   └── internal/               Servicios, autorización y manejadores de eventos
├── infrastructure/
│   ├── persistence/jpa/        Entidades, repositorios, mapeadores y adaptadores
│   ├── authorization/          Principal, filtros de token y límite de solicitudes
│   ├── configuration/          SecurityFilterChain, auditoría y bootstrap
│   ├── identity/               Adaptador remoto y fixtures de demostración
│   ├── hashing/                BCrypt
│   ├── tokens/                 JWT HS256
│   ├── notifications/          Outbox cifrado, SMTP y reintentos
│   └── events/                 Publicador de eventos Spring
└── interfaces/
    ├── rest/                   Controladores, recursos, ensambladores y errores
    └── acl/                    IamContextFacade para otros bounded contexts
```

## Ejecutar localmente

1. Instala/configura **JDK 25**. El repositorio incluye Maven Wrapper.
2. Prepara una base PostgreSQL vacía `saludyadb`. Flyway crea las tablas de IAM y los tres roles.
3. Opcionalmente, desde la raíz ejecuta `docker compose up -d`: PostgreSQL en `localhost:5432`, SMTP de Mailpit en `localhost:1025` y visor de correos en `http://localhost:8025`. Docker es opcional; puedes usar tu PostgreSQL existente y un servidor SMTP de desarrollo.
4. Configura las variables en el entorno del proceso de Spring o en tu IDE.
5. Desde `saludya`, inicia el backend:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_URL = "jdbc:postgresql://localhost:5432/saludyadb"
$env:DB_USER = "saludya"
$env:DB_PASSWORD = "saludya-dev"
$env:IAM_MAIL_ENABLED = "true"
$env:IAM_SMTP_HOST = "localhost"
$env:IAM_SMTP_PORT = "1025"
.\mvnw.cmd spring-boot:run
```

En Linux/macOS: exporta las mismas variables y ejecuta `./mvnw spring-boot:run`.

Abre `http://localhost:8080/swagger-ui.html`. La especificación está en `/v3/api-docs`. Registra un paciente, inicia sesión y copia `accessToken` en **Authorize → bearerAuth**. Usa solo el valor del token en Swagger.

`.env.example` es una referencia. Docker Compose lee `.env`; Spring Boot **no lo carga automáticamente**. No exportes valores de secretos vacíos: omítelos en desarrollo para usar las claves públicas de demostración. Estas claves no sirven para producción.

Los correos se encolan en la misma transacción que el cambio. Para verlos entregados, activa `IAM_MAIL_ENABLED=true` y mantén SMTP disponible. Con el envío desactivado, permanecen en el outbox.

### Identidades sintéticas disponibles

| DNI | Nombre | Apellido | Fecha de nacimiento | Uso |
| --- | --- | --- | --- | --- |
| 71234821 | Lucía | Torres | 1995-04-15 | Paciente adulto y tutor |
| 70000002 | María | Vega | 1992-02-10 | Segundo paciente adulto |
| 87654321 | Diego | Ramos | 1990-06-20 | Personal de admisión |
| 30000001 | Carlos | Vega | 1980-01-12 | Superadministrador inicial |
| 87652716 | Mateo | Torres | 2018-04-15 | Menor vinculado legalmente al DNI 71234821 |
| 87654218 | Sofía | Torres | 2020-08-05 | Menor vinculado legalmente al DNI 71234821 |

Estos datos son fixtures, **no consultas reales a RENIEC**. Solo existen en los perfiles `dev` y `test`, con `iam.identity.mode=fixtures`. No se acepta cualquier DNI.

### Crear el primer superadministrador

No existe registro público para SUPER_ADMIN. El operador activa una provisión inicial explícita:

```powershell
$env:IAM_BOOTSTRAP_ENABLED = "true"
$env:IAM_BOOTSTRAP_EMAIL = "admin@example.test"
$env:IAM_BOOTSTRAP_PASSWORD = "ChooseYourOwn1!Password"
$env:IAM_BOOTSTRAP_DNI = "30000001"
$env:IAM_BOOTSTRAP_PHONE = "987654321"
.\mvnw.cmd spring-boot:run
```

El bootstrap verifica el DNI y la mayoría de edad mediante el proveedor configurado, cifra la clave con BCrypt y reserva la identidad. Si la cuenta ya existe como SUPER_ADMIN, no cambia su contraseña. Si el correo pertenece a otro rol, rechaza el arranque. Desactiva `IAM_BOOTSTRAP_ENABLED` y retira la clave del entorno después de provisionar la cuenta.

En desarrollo, los correos del personal deben pertenecer a `example.test`. Configura `IAM_STAFF_EMAIL_DOMAINS` con los dominios corporativos reales del establecimiento antes de usar otros correos.

## Contrato REST

Los identificadores numéricos de **cuenta**, **paciente** y **vínculo** son distintos. La sesión, el token de recuperación y la solicitud de soporte usan UUID internos.

| Método y ruta | Acceso | Resultado |
| --- | --- | --- |
| POST /api/v1/user-accounts | Público | 201, perfil PatientResource con `id` del paciente y `userId` de la cuenta |
| POST /api/v1/user-accounts/login | Público | 200, `accessToken`, `tokenType`, `expiresAt`, `userId`, `role`, `patientId` |
| POST /api/v1/user-accounts/logout | Sesión autenticada | 204, revoca únicamente la sesión presentada |
| POST /api/v1/user-accounts/recover-password | Público | 202, mensaje genérico; solo cuentas activas reciben un correo |
| POST /api/v1/user-accounts/reset-password | Público, enlace temporal válido | 204, nueva clave y revocación de sesiones |
| POST /api/v1/user-accounts/change-password | Propietario autenticado | 204, exige clave actual e invalida sesiones y enlaces pendientes |
| GET /api/v1/user-accounts/me | Propietario | 200, perfil seguro |
| GET /api/v1/user-accounts/{id} | Propietario o SUPER_ADMIN | 200, perfil seguro |
| PUT /api/v1/user-accounts/{id} | Solo propietario | 200, cambia correo/teléfono |
| POST /api/v1/user-accounts/staff | Solo SUPER_ADMIN | 201, cuenta ADMISSION_STAFF e invitación |
| GET /api/v1/patients/{id} | Propietario, tutor vinculado o personal autorizado | 200, identidad del paciente |
| PUT /api/v1/patients/{id} | Solo titular adulto del perfil | 200, cambia sus datos de contacto |
| GET /api/v1/patients/{id}/minors | Solo titular del perfil tutor | 200, lista de vínculos |
| POST /api/v1/patient-minors | PATIENT adulto | 201, vínculo validado |
| GET /api/v1/patient-minors/{id} | Tutor propietario | 200, vínculo |
| DELETE /api/v1/patient-minors/{id} | Tutor propietario | 204, conserva al paciente |
| POST /api/v1/identity-verifications | Público, limitado por IP | 200, `verified: true` tras contrastar los datos completos |
| GET /api/v1/account-recovery-requests/support | Público | 200, instrucciones y teléfono configurado |
| POST /api/v1/account-recovery-requests | Público | 202, registra solicitud sin conceder acceso ni divulgar una cuenta |
| GET /api/v1/account-recovery-requests | SUPER_ADMIN | 200, hasta 100 solicitudes abiertas |
| POST /api/v1/account-recovery-requests/{id}/resolve | SUPER_ADMIN tras verificar DNI presencialmente | 204, actualiza el correo verificado, revoca sesiones, envía enlace y audita al operador |

Ejemplo de registro:

```json
{
  "dni": "71234821",
  "name": "Lucía",
  "lastname": "Torres",
  "birthDate": "1995-04-15",
  "phone": "987654321",
  "email": "lucia@example.test",
  "password": "Demo1!Password"
}
```

Ejemplo de login:

```json
{"email":"lucia@example.test","password":"Demo1!Password","role":"PATIENT"}
```

También puede usarse `dni` en lugar de `email`. No envíes ambos. El rol seleccionado debe coincidir con el almacenado; el cliente nunca lo asigna a la cuenta.

Ejemplo de recuperación:

```json
{"token":"TOKEN_DEL_CORREO","password":"Updated1!Password","confirmPassword":"Updated1!Password"}
```

La nueva contraseña requiere al menos ocho caracteres, una letra y un número, con un máximo de 72 **bytes UTF-8** por la restricción de BCrypt.

Errores: 400 para formato/validación, 401 para credenciales o sesión inválidas, 403 para permisos, 404 para recursos inexistentes autorizados, 409 para duplicados/conflictos, 422 para identidad/edad/filiación o enlace de recuperación inválido, 429 para exceso de solicitudes y 503 para proveedor o bloqueo temporal no disponible. Los mensajes inesperados no exponen errores SQL ni credenciales.

La verificación previa de identidad no autoriza el registro: el backend vuelve a contrastar los datos al crear la cuenta o el vínculo.

Para una demostración completa, sigue las solicitudes en [iam.http](../saludya/requests/iam.http).

## Base de datos y migraciones

- `V1__create_iam_schema.sql`: roles, usuarios, pacientes, vínculos, perfiles del personal, sesiones, recuperación, outbox y solicitudes de soporte.
- `V2__reserve_account_identities.sql`: reserva única de DNI entre cuentas de pacientes y del personal.
- `patients.id_user` es **nullable**: un menor tiene un perfil sin credenciales. La restricción de unicidad sigue aplicándose a titulares con cuenta.
- Los datos del personal se almacenan en `staff_profiles`; no se crean pacientes ficticios para personal administrativo.
- Identidad y perfil clínico permanecen al retirar un vínculo. El menor deja de ser administrable por el tutor y no se resuelve su correo de contacto.
- Flyway crea el esquema; Hibernate solo lo valida. No se utiliza `ddl-auto=update`.

### Si ya ejecutaste el SQL original del README

La opción más sencilla para una demostración es una **base nueva y vacía**. No ejecutes el script histórico además de Flyway.

Para una base existente, realiza primero un respaldo y ensaya la migración sobre una copia. V1 adapta las cuatro tablas originales de IAM: identificadores BIGINT, auditoría y `id_user` nullable. No elimina tablas de otros contextos.

Solo después de comprobar la copia, permite que Flyway registre un baseline **0**:

```powershell
$env:SPRING_FLYWAY_BASELINE_ON_MIGRATE = "true"
$env:SPRING_FLYWAY_BASELINE_VERSION = "0"
.\mvnw.cmd spring-boot:run
```

Retira estas variables una vez creada la historia de Flyway. No uses baseline 1: omitiría V1. No ejecutes los archivos de migración manualmente sobre la base productiva.

Antes de migrar, revisa correos que solo difieran en mayúsculas y perfiles anteriores sin fecha de nacimiento verificada. La normalización no puede corregir por sí sola identidades incompletas ni resolver duplicados. Si existen cuentas de personal históricas sin `staff_profiles`, deben completarse y validarse con el proveedor. Las migraciones no inventan información de identidad.

Los otros bounded contexts necesitan sus propias migraciones al integrarse en `develop`; esta rama no incorpora las implementaciones de Booking, Arrival, Reassignment ni Hospital Configuration.

## Integración con los demás contextos

Usa `IamContextFacade` en lugar de acceder a entidades/repositorios JPA de IAM:

- `getUserById(accountId)`: correo, rol y estado; nunca hash ni token.
- `getPatientById(patientId)`: identidad y perfil.
- `getMinorsByTutor(tutorPatientId)`: perfiles de menores vinculados.
- `canManagePatient(accountId, patientId)`: exige titular activo PATIENT y comprueba propiedad o vínculo.
- `getContactEmailForPatient(patientId)`: correo del titular activo o del tutor actual.

Los DTO de la fachada son internos. Cada endpoint de Booking/Arrival/Reassignment debe comprobar permisos de operación y propiedad antes de devolver información o aceptar un `patientId`. Un JWT válido por sí solo no autoriza operar sobre cualquier paciente.

La cadena de seguridad exige autenticación para los demás endpoints. `/api/v1/config/**` y las rutas alternativas de configuración requieren SUPER_ADMIN, siguiendo el controlador existente en `hospitalconfig`. Los permisos específicos y la propiedad de recursos de otros contextos deben incorporarse al integrarlos.

## Producción y servicios externos

Ejecuta con `SPRING_PROFILES_ACTIVE=prod`. Configura:

| Variables | Propósito |
| --- | --- |
| DB_URL, DB_USER, DB_PASSWORD | PostgreSQL del despliegue |
| IAM_JWT_SECRET | Clave independiente, impredecible y de al menos 32 bytes |
| IAM_NOTIFICATION_KEY | Base64 de 32 bytes aleatorios, para AES-256-GCM |
| IAM_IDENTITY_URL, IAM_IDENTITY_API_KEY | Gateway de identidad confiable |
| IAM_STAFF_EMAIL_DOMAINS | Lista separada por comas de dominios corporativos permitidos |
| IAM_PASSWORD_RESET_URL | URL HTTPS real del frontend que captura el token y llama a reset-password |
| IAM_SMTP_HOST, IAM_SMTP_PORT, IAM_SMTP_USER, IAM_SMTP_PASSWORD, IAM_MAIL_FROM | Entrega real de correo con autenticación y STARTTLS |
| IAM_SUPPORT_PHONE, IAM_SUPPORT_INSTRUCTIONS | Teléfono y canales reales de la mesa de ayuda |
| IAM_CORS_ORIGINS | Orígenes web permitidos explícitamente |
| IAM_SWAGGER_ENABLED | Opcional, false por defecto en producción |

No se incluyen claves reales de RENIEC ni de SMTP. No se ha afirmado que los fixtures sean una integración oficial. Sin proveedor confiable, la validación de identidad falla y no permite registrar cuentas.

El adaptador `ReniecService` consume un **gateway normalizado**, no presupone que cualquier API comercial de DNI tenga los mismos campos. La URL contiene `{dni}`, por ejemplo `https://identity.hospital.example/identities/{dni}`. Envía `Authorization: Bearer <IAM_IDENTITY_API_KEY>` si se configura la clave.

Respuesta esperada:

```json
{
  "dni": "71234821",
  "name": "Lucía",
  "lastname": "Torres",
  "birthDate": "1995-04-15",
  "guardianDnis": []
}
```

Para un menor, `guardianDnis` contiene los DNI de tutores verificados. Si el proveedor no ofrece fecha de nacimiento o filiación, debe completarse la integración con una fuente autorizada; no se acepta sustituir esos datos por una afirmación del cliente. Respuestas incompletas o fallos remotos retornan 503; identidad no encontrada retorna 422. El adaptador usa tiempos máximos de conexión/lectura.

En recuperación asistida, `identityCheckedInPerson=true` es una declaración del operador SUPER_ADMIN tras revisar el DNI original y confirmar el correo de destino con el titular. La API no sustituye ese procedimiento presencial. El ID del operador y la fecha quedan registrados. No expongas este permiso a pacientes ni personal sin esa atribución.

El outbox cifra los cuerpos antes de guardarlos, reintenta el envío y marca FAILED tras diez fallos. Tras enviar, elimina el contenido original del cuerpo, incluidos enlaces. La entrega SMTP es **al menos una vez**: si el servidor acepta un correo pero el proceso cae antes del commit, puede reenviarlo. Conserva la clave de cifrado mientras existan correos pendientes y configura monitoreo de PENDING/FAILED.

La limitación por IP es local al proceso (30 solicitudes sensibles por minuto por ruta). Para varias réplicas, aplica un límite compartido en el gateway. Usa HTTPS en el ingreso del despliegue, restringe CORS, conserva los secretos fuera del repositorio y establece una política de retención para sesiones caducadas, tokens y solicitudes resueltas.

## Pruebas y evidencia

```powershell
cd saludya
.\mvnw.cmd -B -ntp verify
```

La suite usa H2 con las **mismas migraciones**. Para ejecutar la suite contra una base PostgreSQL de pruebas **vacía y exclusiva**:

```powershell
$env:IAM_TEST_DB_URL = "jdbc:postgresql://localhost:5432/iam_tests"
$env:IAM_TEST_DB_USER = "iam_test"
$env:IAM_TEST_DB_PASSWORD = "YOUR_TEST_DATABASE_PASSWORD"
$env:IAM_TEST_DB_DRIVER = "org.postgresql.Driver"
.\mvnw.cmd -B -ntp verify
```

Los tests limpian las tablas de IAM entre casos: nunca apuntes estas variables a una base de trabajo o producción.

La suite JUnit comprueba los casos de la tabla inicial, los límites de BCrypt, la autenticidad del cifrado, el proveedor remoto mediante un servidor HTTP local, el calendario peruano para mayoría de edad, SMTP con fallos/reintentos y Swagger. El workflow `iam-verify.yml` ejecuta H2 y PostgreSQL y conserva los informes Surefire.

Las especificaciones en `src/test/resources/features/iam.feature` describen la aceptación del negocio. La ejecución automatizada real se realiza con JUnit; no se presentan esos archivos como tests Cucumber ejecutados.

Validación local de esta entrega: Maven verify con H2 y PostgreSQL 18, migraciones sobre el esquema vacío original del README y arranque del JAR con PostgreSQL. También se verificó registro, login con reloj real, bootstrap administrativo y recuperación entregada a un servidor SMTP local de captura. La validación con proveedores externos reales requiere credenciales del establecimiento.

Para el Sprint Review puedes mostrar, en orden: registro y bienvenida; login y perfil; menor vinculado/desvinculado; recuperación y rechazo al reutilizar el enlace; alta de personal desde SUPER_ADMIN; intento de operación sin permiso; informe de pruebas. La incorporación de esas evidencias al reporte permanece como una tarea de documentación independiente.
