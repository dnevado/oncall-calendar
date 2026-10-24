# Research: Registro de cambios y cesiones de guardia

**Feature**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Fecha**: 2026-10-24

Decisiones de fase 0. Las marcadas **(usuario)** las tomó el responsable del proyecto el 2026-10-24; el resto son decisiones técnicas razonadas con evidencia del legacy.

## R1. Despliegue e infraestructura (usuario)

- **Decision**: contenedores Docker desplegados junto al legacy, detrás de un proxy inverso (nginx) en el mismo dominio. Rutas: `/app/` → frontend estático, `/api/` → backend nuevo, resto → legacy. La infraestructura como código es un `docker-compose.yml` más la configuración del proxy, en `deploy/`.
- **Rationale**: el legacy y su MySQL 5.7 están autoalojados (dump: `5.7.33-0ubuntu0.16.04.1`); estar en la misma red evita exponer la base de datos y simplifica la convivencia. El mismo dominio evita CORS y permite cookies `SameSite=Strict`.
- **Alternatives considered**: nube gestionada (requiere túnel o VPN hasta la MySQL autoalojada durante toda la convivencia); el intento previo con AWS Lambda quedó descartado por el usuario.
- **Pendiente operativo (no bloquea el plan)**: cómo encaja el proxy con la entrada actual del servidor de producción. Se valida en el despliegue.

## R2. Autenticación (usuario)

- **Decision**: login propio del backend nuevo contra `medicos` (`Email`, `Password`), con las mismas condiciones que el legacy: `Activo = 1` y `Confirmado = 1` (`web/login.jsp:237`). El legacy guarda **SHA-512 sin sal en Base64** (`src/guardias/security/SecurityUtil.java:71-100`). En el primer login correcto, el backend guarda un **hash adaptativo** (Argon2id, o bcrypt si no está disponible) en columnas nuevas, junto con la huella del hash legacy usado.
  - Si `medicos.Password` cambia después (cambio de contraseña en el legacy), la huella deja de coincidir: el hash nuevo se descarta y se vuelve a verificar contra el legacy.
  - La sesión es del lado del servidor, con una cookie propia `HttpOnly`, `Secure` y `SameSite=Strict` (nombre distinto de `JSESSIONID`), y protección CSRF para la SPA.
  - **Autorización:** el rol de administrador por servicio sale de `guardias_servicios_medicos.IsAdministrator = 1` (igual que `MedicoDBImpl.getMedicosAdministradores`).
- **Rationale**: cumple el principio VI (hash adaptativo) sin romper el login del legacy y sin acoplarse a su sesión.
- **Alternatives considered**: reutilizar la sesión del legacy (acopla ambos sistemas y hereda SHA-512); un IdP externo con OIDC (es un proyecto en sí mismo).
- **Riesgo aceptado**: mientras conviva el legacy, el hash SHA-512 sin sal sigue existiendo en `medicos.Password`. Se elimina al retirar el login del legacy.

## R3. Integración de la interfaz (usuario)

- **Decision**: aplicación React independiente en `/app/`, con su propio login. La pantalla de cambios del legacy se desactiva **sin tocar código**, fijando `ACTIVAR_CAMBIO_GUARDIAS = 'N'` en `configuracion` para el servicio: la prevalidación del legacy rechaza entonces cualquier cambio (`web/medico/cambio_guardia.jsp:144`). El enlace desde el menú del legacy es un cambio mínimo de una línea en `web/common/navigation.jsp`, que **requiere aprobación** (principio III; ver Complexity Tracking).
- **Rationale**: aislamiento total de sesión, estilos y pruebas.
- **Alternatives considered**: embeberla en la JSP (iframe o micro-frontend), descartado por complejidad.

## R4. Ubicación del código y herramientas

- **Decision**:
  - `backend/`: Maven, Spring Boot 3.x, Java 21.
  - `frontend/`: Vite, React 18, TypeScript estricto.
  - `contracts/guardias-cambios.openapi.yaml`: contrato publicado.
  - `deploy/`: compose y proxy.
  - El legacy (`src/`, `web/`, `resources/`, `build/`) no se mueve.
- **Rationale**: la constitución fija la tecnología y la ubicación de los contratos. Maven porque el legacy no usa ninguna herramienta de build (proyecto Eclipse con jars en `WEB-INF/lib`) y es la opción más habitual en Spring. Vite porque Create React App está abandonado.
- **Alternatives considered**: Gradle (válido, sin ventaja para el equipo); monorepo con workspaces (innecesario para dos aplicaciones).

## R5. Acceso a datos en el backend nuevo

- **Decision**: MyBatis (`mybatis-spring-boot-starter` 3.x) con SQL explícito y parámetros enlazados, y `@Transactional` de Spring.
- **Rationale**: el esquema legacy no se presta a JPA: `guardias_cambios` no tiene PK, las fechas son `varchar` y el tipo de guardia de los residentes es `''`. Además, MyBatis da continuidad al conocimiento del equipo y la SQL queda visible para revisar la paridad.
- **Alternatives considered**: JPA/Hibernate (mapeo forzado del esquema legacy); `JdbcClient` de Spring (válido, pero sin mappers declarativos).

## R6. Migraciones de esquema sobre la base de datos legacy

- **Decision**: Liquibase, con changesets solo aditivos y compatibles con el legacy (columnas nullable, tablas nuevas, índices, trigger). El primer despliegue sobre la base existente se hace sin recrear nada.
- **Rationale**: la base es MySQL 5.7, que Liquibase sigue soportando; Flyway Community ha ido retirando el soporte de versiones de MySQL fuera de mantenimiento. Debe verificarse la versión concreta al implementar.
- **Alternatives considered**: Flyway (riesgo de compatibilidad con 5.7); SQL manual (sin trazabilidad).
- **Ejecución**: Liquibase **no se ejecuta al arrancar en producción** (perfil `prod`: `spring.liquibase.enabled=false`). En producción se lanza explícitamente desde el runbook (T065), después de la copia de seguridad y la limpieza confirmada. En local y en pruebas sí se ejecuta al arrancar.
- **Riesgo**: MySQL 5.7 está fuera de soporte desde octubre de 2023. Su actualización queda fuera de alcance, pero debe planificarse.

## R7. Identificador de `guardias_cambios`

- **Decision**:
  1. Limpiar los 3 `IdCambio` duplicados, todos inserciones manuales (discovery D3).
  2. Añadir `PRIMARY KEY (IdCambio)` y `AUTO_INCREMENT`.
  3. El backend nuevo deja que la base asigne el ID.
  4. El legacy sigue insertando con ID explícito (`max+1`), que MySQL acepta con `AUTO_INCREMENT`, aunque su pantalla queda desactivada (R3).
- **Rationale**: principio VII (prohibido `max(id)+1`).
- **Alternatives considered**: una tabla de secuencia propia (dos mecanismos conviviendo); UUID (incompatible con la columna `int` que lee el legacy).
- **Requiere autorización explícita** (constitución, flujo 6): la limpieza modifica datos de producción. Se hará como tarea separada, con copia previa y script revisado.

## R8. Unicidad de guardias (FR-025)

- **Decision**: índice único `(IdMedico, FGuardia(10), IdServicio)` en `guardias_medicos`. Prefijo de 10 caracteres porque `FGuardia` es `varchar(1000)` y todas las fechas tienen formato `yyyy-MM-dd` (discovery §8: 0 filas con otro formato). Antes hay que resolver los 3 casos duplicados (D9), con la misma autorización que R7.
- **Riesgo de regresión**: si el generador de agenda del legacy (`calc_agenda.jsp`) llega a insertar duplicados, fallará con el índice. Hace falta una prueba de regresión del generador sobre un mes de prueba antes de crear el índice en producción.

## R9. Concurrencia y atomicidad (FR-021, caso «cambios simultáneos»)

- **Decision**: una transacción por cambio, con bloqueo `SELECT … FOR UPDATE` de las filas de `guardias_medicos` afectadas (origen, destino y guardias del mismo día de cedente y receptor, en orden determinista de `Id` para evitar interbloqueos). Todas las reglas se evalúan dentro de la transacción, después del bloqueo. El índice único de R8 es la última barrera.
- **Rationale**: InnoDB soporta transacciones y bloqueos de fila; el legacy no los usaba.
- **Idempotencia**: cabecera opcional `Idempotency-Key` en el alta. Una repetición con la misma clave devuelve el resultado original sin aplicar nada (principio VII). Sin clave, un cambio repetido se rechaza igualmente por FR-005 y FR-010.

## R10. Persistencia de una CESION en la tabla legacy

- **Decision**: en una CESION, `FechaFinCambio` se guarda **igual que `FechaIniCambio`** (la fecha en que el receptor queda de guardia). El dominio y la API no exponen fecha destino para la cesión (FR-003). En columnas nuevas se guardan tipo y festivo de la guardia cedida (FR-023).
- **Rationale**: la consulta legacy de cesiones hechas (`CambiosGuardias.xml:9-24`) une `FechaFinCambio` con la guardia del receptor. Con `FechaFin = FechaIni`, esa guardia es justo la cedida, así que el legacy, **sin cambiar su código**, cuenta la cesión para el cedente con el tipo y el festivo correctos. Con `NULL`, la cesión no se contaría en absoluto. La consulta de cesiones disfrutadas sigue sin descontarla al receptor, que es su comportamiento actual (fuera de alcance, FR-023).
- **Alternatives considered**: `NULL` (empeora el equilibrio del legacy); emular una fecha destino ficticia (no tiene significado).
- **Validación**: SC-006 se comprueba ejecutando las consultas legacy de equilibrio sobre un conjunto de datos fijo antes y después del despliegue.

## R11. Histórico de tipo de médico (FR-024)

- **Decision**: tabla nueva `medicos_tipo_historico` (médico, tipo, subtipo, desde, hasta), alimentada por un **trigger** `AFTER UPDATE` sobre `medicos` cuando cambian `Tipo` o `SubTipo`, de modo que los cambios hechos desde el legacy también quedan registrados sin tocar su código. Carga inicial: el tipo actual de cada médico con `desde = NULL` («desde siempre», según el supuesto de la spec).
- **Rationale**: como los cambios solo se admiten sobre fechas futuras (FR-008, FR-009), el tipo vigente en la fecha de la guardia es el último conocido salvo cambios programados. El trigger cubre las ediciones hechas desde el legacy.
- **Riesgo**: crear triggers en MySQL con binlog activo puede exigir `log_bin_trust_function_creators` o privilegios adicionales; se verifica en el entorno de pruebas.
- **Nota**: para FR-013, el tipo es ADJUNTO o RESIDENTE. El `SubTipo` de los adjuntos (R1/R5) es ruido de datos (D13) y se ignora. Para FR-020 solo cuentan los residentes con subtipo R3, R4 o R5. «Residente simulado» es `SubTipo = 'SIMULADO'`.

## R12. Pruebas

- **Decision**:
  - **Backend:** JUnit 5 y Spring Boot Test. Integración con **Testcontainers MySQL 5.7**, la versión de producción, cargando el DDL extraído del dump (solo esquema, sin datos).
  - **Contrato:** validación de peticiones y respuestas contra el OpenAPI publicado.
  - **Frontend:** Vitest, Testing Library y MSW con respuestas derivadas del contrato.
  - **Regresión legacy:** pruebas que ejecutan la SQL legacy de calendario y de equilibrio (copiada literalmente de `CambiosGuardias.xml` y `GuardiasDBImpl.java`) para SC-006 y SC-008.
- **Nota**: el entorno local del usuario usa MariaDB (cambios locales en `ConfigurationIBATIS.xml`). Las pruebas fijan MySQL 5.7 para tener paridad con producción.

## R13. Desactivación de la pantalla de cambios del legacy

- **Decision**: `ACTIVAR_CAMBIO_GUARDIAS = 'N'` para el servicio, en el momento de la puesta en marcha (ver R3). El backend nuevo ignora esa clave (CG-001 descartada).
- **Limitación conocida**: `add_cambio_guardia.jsp` no vuelve a comprobar la clave (V2). Quien llame directamente a ese endpoint podría registrar un cambio en el legacy. Se acepta durante la convivencia porque solo es alcanzable por un usuario autenticado del legacy; desaparece al retirar el legacy.

## R14. Gestión de secretos (usuario)

- **Decision**: secretos de Docker. Las credenciales de la base (y cualquier secreto futuro) se montan como ficheros en `/run/secrets/` del contenedor del backend, con permisos restringidos, y Spring Boot los lee con `spring.config.import=optional:configtree:/run/secrets/`. Los ficheros de origen viven en el servidor, fuera del repositorio (`deploy/secrets/`, ignorado por git); el repositorio solo documenta sus nombres.
- **Rationale**: cumple el principio VI (gestor de secretos, sin secretos en variables de entorno planas ni en el repositorio) sin añadir infraestructura al piloto.
- **Alternatives considered**: un gestor externo tipo Vault (pieza de infraestructura nueva); un `.env` como excepción temporal (incumple el principio VI).

## R15. Limitación de intentos de login (usuario)

- **Decision**: 5 intentos fallidos consecutivos por cuenta → 15 minutos de bloqueo en la aplicación nueva (FR-029). El contador y el fin del bloqueo se guardan en columnas nuevas de `medicos` (`LoginFallidosV2`, `BloqueadoHastaV2`), que persisten entre reinicios. La respuesta es la misma 401 tanto si la cuenta existe como si no.
- **Rationale**: protección básica contra fuerza bruta, sin afectar al login del legacy.
