# Identity & Access Management — SaludYa

**Responsable: Didier**

El módulo de Identity & Access Management administra las cuentas y el acceso a SaludYa. Su desarrollo comprende el registro de pacientes, el inicio de sesión, la gestión de perfiles, la vinculación de menores y la recuperación de cuentas. También incluye el registro del personal de admisión, a cargo del superadministrador.

La implementación corresponde a las historias US-01 a US-06 y a la estructura definida en la sección 2.6.1 del reporte. Se desarrolló con Java 25, Spring Boot 4.1.1 y PostgreSQL, y se encuentra en la rama `iam`.

## Funcionalidades desarrolladas

| Historia | Implementación |
| --- | --- |
| US-01: Registro de pacientes | Registro de titulares mayores de edad, validación de DNI y datos personales, control de correos duplicados, almacenamiento de contraseñas con BCrypt y notificación de bienvenida. |
| US-02: Registro del personal administrativo | Creación de cuentas ADMISSION_STAFF por un SUPER_ADMIN, validación de identidad y correo corporativo, y envío de una invitación para establecer la contraseña. |
| US-03: Vinculación de menores | Registro del perfil del menor, verificación de filiación, asociación con un tutor adulto y desvinculación sin eliminar el perfil del paciente. |
| US-04: Recuperación de cuentas | Recuperación mediante un enlace de un solo uso con vigencia de 15 minutos. Registro de solicitudes de atención presencial cuando el titular pierde acceso a sus medios de contacto. |
| US-05: Edición del perfil | Actualización del correo y teléfono del propietario de la cuenta, manteniendo el DNI, los datos de identidad y el rol. |
| US-06: Inicio de sesión | Autenticación mediante correo o DNI, contraseña y rol seleccionado. Emisión de JWT, control de sesiones, validación de cuentas activas y cierre de sesión. |

En el registro de personal se optó por enviar un enlace temporal para establecer la contraseña. El correo contiene la información de acceso, mientras que la clave queda definida por el usuario.

Los menores cuentan con un perfil de paciente y acceden a los servicios mediante su tutor. No reciben una cuenta de inicio de sesión propia. Un vínculo existente no se transfiere automáticamente a otro titular; ese caso requiere la intervención del área de admisión.

## Organización del módulo

El código se organizó en las cuatro capas establecidas para el backend:

| Capa | Responsabilidad |
| --- | --- |
| `domain` | Agregados UserAccount y Patient, entidades PatientMinor y StaffProfile, objetos de valor, roles, eventos y contratos de persistencia. |
| `application` | Casos de uso de registro, autenticación, perfiles, recuperación y vínculos de menores. Incluye los servicios de comandos, consultas y las reglas de autorización. |
| `infrastructure` | Persistencia con JPA, BCrypt, JWT, validación de identidad, envío de correos, configuración de seguridad y auditoría. |
| `interfaces` | Controladores REST, recursos de entrada y salida, ensambladores, manejo de errores y fachada de comunicación con otros contextos. |

El módulo se encuentra en `saludya/src/main/java/com/ruwalabs/saludya/iam/`. Los modelos del dominio se mantienen separados de las entidades utilizadas por JPA.

## Roles y control de acceso

Se implementaron los roles PATIENT, ADMISSION_STAFF y SUPER_ADMIN. El registro público asigna el rol PATIENT. Las cuentas del personal de admisión se crean desde una sesión de SUPER_ADMIN y utilizan los dominios de correo corporativo definidos en la configuración.

El primer superadministrador se provisiona mediante el mecanismo de inicialización del backend. Su creación requiere una identidad verificada y datos configurados por el administrador del sistema. Este mecanismo conserva la contraseña de una cuenta administrativa existente.

Cada solicitud autenticada comprueba la firma y vigencia del JWT, el estado de la cuenta y la sesión almacenada. Al cerrar sesión se revoca el token utilizado. Un cambio o restablecimiento de contraseña invalida las sesiones anteriores; el cambio de contraseña también invalida los enlaces de recuperación pendientes.

La consulta y edición de perfiles se restringen según el rol y la propiedad del recurso. Un paciente puede consultar su perfil y los menores que mantiene vinculados. La edición de datos de contacto corresponde al titular de la cuenta.

## Servicios REST

Los servicios se publicaron bajo `/api/v1`. La especificación OpenAPI describe los recursos y parámetros de cada operación.

| Método | Ruta | Operación |
| --- | --- | --- |
| POST | `/user-accounts` | Registrar un paciente. |
| POST | `/user-accounts/login` | Iniciar sesión. |
| POST | `/user-accounts/logout` | Cerrar la sesión actual. |
| GET | `/user-accounts/me` | Consultar el perfil de la sesión actual. |
| GET | `/user-accounts/{id}` | Consultar una cuenta propia o autorizada para SUPER_ADMIN. |
| PUT | `/user-accounts/{id}` | Actualizar los datos de contacto propios. |
| POST | `/user-accounts/staff` | Registrar personal de admisión desde SUPER_ADMIN. |
| POST | `/user-accounts/recover-password` | Solicitar un enlace de recuperación. |
| POST | `/user-accounts/reset-password` | Establecer una contraseña mediante el enlace recibido. |
| POST | `/user-accounts/change-password` | Cambiar la contraseña utilizando la clave actual. |
| GET | `/patients/{id}` | Consultar un perfil de paciente autorizado. |
| PUT | `/patients/{id}` | Actualizar los datos de contacto del paciente titular. |
| GET | `/patients/{id}/minors` | Consultar los vínculos del tutor titular. |
| POST | `/patient-minors` | Vincular un menor. |
| GET | `/patient-minors/{id}` | Consultar un vínculo propio. |
| DELETE | `/patient-minors/{id}` | Desvincular un menor. |
| POST | `/identity-verifications` | Contrastar el DNI y los datos personales. |
| GET | `/account-recovery-requests/support` | Consultar los canales de atención configurados. |
| POST | `/account-recovery-requests` | Registrar una solicitud de recuperación asistida. |
| GET | `/account-recovery-requests` | Consultar solicitudes abiertas desde SUPER_ADMIN. |
| POST | `/account-recovery-requests/{id}/resolve` | Aprobar una recuperación tras verificar presencialmente la identidad. |

El inicio de sesión recibe correo o DNI, contraseña y rol. El rol seleccionado debe coincidir con el registrado en la cuenta. La respuesta contiene el token de acceso, su fecha de vencimiento y los identificadores del usuario y del paciente, cuando corresponde.

Los identificadores de cuenta, paciente y vínculo son independientes. Las respuestas de consulta excluyen los hashes de contraseña y los tokens de recuperación.

Los ejemplos de solicitudes se encuentran en [iam.http](../saludya/requests/iam.http). La documentación interactiva está disponible en `/swagger-ui.html` dentro del entorno de desarrollo.

## Persistencia

El esquema de IAM se administra mediante Flyway. La primera migración crea las tablas del módulo y contempla la adaptación de las cuatro tablas de IAM del esquema original del README. La segunda incorpora la reserva única de DNI entre las cuentas de pacientes y del personal.

| Tabla | Información almacenada |
| --- | --- |
| `roles` | Roles disponibles. |
| `users` | Credenciales, rol y estado de las cuentas. |
| `patients` | Identidad y datos del paciente. |
| `patient_minors` | Relación entre un menor y su tutor. |
| `staff_profiles` | Identidad y datos del personal administrativo. |
| `identity_claims` | Reserva del DNI asociado a una cuenta. |
| `user_sessions` | Vigencia y revocación de sesiones. |
| `password_reset_tokens` | Hash, vencimiento y estado de los enlaces de recuperación. |
| `iam_notification_outboxes` | Correos pendientes, intentos de envío y estado de entrega. |
| `account_recovery_requests` | Solicitudes de recuperación asistida y datos de su aprobación. |

Se permitió que `patients.id_user` sea nulo para representar a los menores sin crear credenciales de acceso. La eliminación de un vínculo conserva el perfil del menor.

Hibernate valida el esquema generado por las migraciones. La adaptación de bases existentes requiere revisar previamente los duplicados y los registros de identidad incompletos. La compatibilidad se comprobó sobre el esquema vacío original del README; no se realizó una migración de datos de producción.

## Recuperación y notificaciones

La recuperación por correo utiliza un token aleatorio cuyo hash se almacena en la base de datos. El enlace vence a los 15 minutos y solo admite un uso. La respuesta de solicitud es la misma para un correo registrado y uno inexistente, de modo que no revela qué cuentas pertenecen al sistema.

La recuperación asistida registra el DNI y el correo de contacto indicado por el solicitante. La aprobación corresponde a un SUPER_ADMIN después de revisar presencialmente el documento y confirmar el correo con el titular. Se registra el identificador del administrador y la fecha de aprobación, se revocan las sesiones y se envía un enlace para establecer la nueva contraseña.

Las notificaciones se guardan en la misma transacción que la operación que las genera. Sus cuerpos se cifran con AES-256-GCM y el envío se realiza mediante SMTP. El servicio conserva los mensajes pendientes para reintentar la entrega y los marca como fallidos después de diez intentos. Una vez enviado un correo, se retira el contenido original de su cuerpo almacenado. La entrega puede repetirse si el proceso se interrumpe después de que SMTP acepta el mensaje y antes de confirmar la transacción.

## Comunicación con otros contextos

La fachada `IamContextFacade` concentra las consultas que necesitan los demás módulos del backend. Permite obtener usuarios, perfiles de pacientes, menores vinculados y el correo de contacto del titular o tutor. También incorpora `canManagePatient`, que comprueba si una cuenta activa puede gestionar un paciente.

La fachada entrega datos de consulta sin exponer las entidades JPA ni las credenciales. La integración con Booking, Arrival y Reassignment queda sujeta a la validación de permisos y propiedad de los recursos en sus respectivos servicios.

## Validación realizada

Se desarrollaron 45 pruebas automatizadas con JUnit. La suite se ejecutó con H2 y PostgreSQL 18, utilizando las mismas migraciones. En ambos entornos se obtuvo un resultado de 45 pruebas aprobadas, sin fallos ni errores.

Las pruebas cubren el registro, la mayoría de edad, las credenciales y roles, el acceso a perfiles ajenos, los vínculos de menores, la recuperación de contraseñas y el envío de notificaciones. También comprueban el comportamiento ante solicitudes simultáneas: el registro de un mismo DNI crea una sola cuenta y un enlace de recuperación solo puede utilizarse una vez.

Además, se verificó el arranque del JAR con PostgreSQL, el acceso con JWT utilizando el reloj real, la provisión del superadministrador y la entrega de un correo de recuperación a un servidor SMTP local. El enlace recibido permitió cambiar la contraseña, revocó la sesión anterior y rechazó su reutilización.

El workflow `iam-verify.yml` ejecutó las pruebas en GitHub Actions con H2 y PostgreSQL y conservó los informes de resultados. Las especificaciones de aceptación se encuentran en `src/test/resources/features/iam.feature`; la ejecución automatizada corresponde a los tests JUnit.

## Estado de las integraciones

El entorno de desarrollo utiliza identidades sintéticas para comprobar los flujos del módulo. El adaptador `ReniecService` está preparado para consumir un servicio de identidad con DNI, nombres, apellidos, fecha de nacimiento y datos de filiación. La conexión y validación con un proveedor real quedan pendientes de su contrato y credenciales.

La configuración separa los perfiles de desarrollo y producción. Las credenciales de PostgreSQL, las claves de seguridad, el servicio SMTP, los dominios corporativos y los canales de soporte se reciben mediante variables de entorno. La configuración de referencia se encuentra en `.env.example`.

La recuperación implementada utiliza correo electrónico. La verificación por SMS/OTP representada en los mockups queda pendiente de implementación e integración con un proveedor.
