---

description: "Task list for feature 001 — Registro de cambios y cesiones de guardia"
---

# Tasks: Registro de cambios y cesiones de guardia

**Input**: Design documents from `/specs/001-registro-cambios-guardia/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/guardias-cambios.openapi.yaml), [quickstart.md](./quickstart.md)

**Tests**: incluidas. La constitución (principio V) exige al menos una prueba automática en el servidor por regla del catálogo, y pruebas de integración contra base de datos para las reglas críticas; [quickstart.md](./quickstart.md) enumera las pruebas mínimas. Dentro de cada historia, las pruebas se escriben primero y deben fallar antes de implementar.

**Organization**: tareas agrupadas por historia de usuario para implementarlas y probarlas de forma independiente.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede ejecutar en paralelo (ficheros distintos, sin dependencias pendientes)
- **[Story]**: historia de usuario de la spec (US1–US4)

## Path Conventions

- Backend: paquete raíz `es.medoncalls.guardias` en `backend/src/main/java/es/medoncalls/guardias/`; pruebas en `backend/src/test/java/es/medoncalls/guardias/`; SQL MyBatis en `backend/src/main/resources/mappers/`; Liquibase en `backend/src/main/resources/db/changelog/`.
- Frontend: `frontend/src/`.
- Contrato publicado: `contracts/guardias-cambios.openapi.yaml`.
- Despliegue: `deploy/`.
- **Legacy (`src/`, `web/`, `resources/`, `build/`)**: no se modifica, salvo T066 (aprobado en el plan).

## Reglas transversales para quien implemente

- Ningún dato de producción en pruebas, fixtures ni documentos; datos ficticios (principio VI). `bbdd/` solo se usa en T004, para extraer el DDL sin filas.
- Toda SQL con parámetros enlazados (`#{}` en MyBatis, nunca `${}` con entradas de usuario) (principio VII).
- Los mensajes y textos para el usuario en español y en UTF-8.
- Cada regla de validación devuelve su código del catálogo (`CG-005`, … o `MISMO_MEDICO`), tal y como fija el contrato.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: inicializar los proyectos nuevos y su configuración básica.

- [X] T001 Create Maven project `backend/pom.xml` (Spring Boot 3.x parent, Java 21; dependencies: spring-boot-starter-web, -security, -validation, mybatis-spring-boot-starter 3.x, liquibase-core, mysql-connector-j, spring-security-crypto with bouncycastle `bcprov` for Argon2; test: spring-boot-starter-test, spring-security-test, testcontainers `mysql` + `junit-jupiter`, `com.atlassian.oai:swagger-request-validator-mockmvc`) and entry point `backend/src/main/java/es/medoncalls/guardias/GuardiasApplication.java`; configuration in `backend/src/main/resources/application.yml` with datasource properties read from Docker secrets via `spring.config.import=optional:configtree:/run/secrets/` (files `guardias_db_url`, `guardias_db_user`, `guardias_db_password`; R14 — never plain environment variables), a `prod` profile in `backend/src/main/resources/application-prod.yml` with `spring.liquibase.enabled=false` (R6), `guardias.zona-horaria=Europe/Madrid`, MyBatis `mapper-locations: classpath:mappers/*.xml` and `auto-mapping-unknown-column-behavior: NONE`
- [X] T002 [P] Create Vite + React 18 + TypeScript project in `frontend/` (`frontend/package.json`, `frontend/tsconfig.json` with `"strict": true`, `frontend/vite.config.ts` with `base: '/app/'` and dev proxy `/api` → `http://localhost:8080`, `frontend/index.html`, `frontend/src/main.tsx`); add Vitest, @testing-library/react, @testing-library/user-event, msw, openapi-typescript, openapi-fetch and scripts `dev`, `build`, `test`, `lint`, `gen:api`
- [X] T003 [P] Publish the contract: copy `specs/001-registro-cambios-guardia/contracts/guardias-cambios.openapi.yaml` to `contracts/guardias-cambios.openapi.yaml` and set `info.version: 1.0.0` (principle IV)
- [X] T004 [P] Extract the legacy schema without data into `backend/src/test/resources/legacy-schema.sql`: from the local dump `bbdd/Guardias_prod_2026-08-26.db` copy only the `CREATE TABLE` statements for `configuracion`, `guardias_cambios`, `guardias_medicos`, `guardias_servicios`, `guardias_servicios_medicos`, `medicos`, `vacaciones_medicos` (no `INSERT`, no comments with data); verify with `grep -c INSERT` = 0 before committing
- [X] T005 [P] Configure linting/formatting: `frontend/eslint.config.ts` (TypeScript config, no new JavaScript files) and `frontend/.prettierrc` (TypeScript + React rules), root `.editorconfig` (UTF-8, LF, final newline)
- [X] T006 [P] Add ignores to root `.gitignore`: `backend/target/`, `frontend/node_modules/`, `frontend/dist/`, `deploy/secrets/`
- [X] T007 [P] Create deployment skeleton: `deploy/docker-compose.yml` (services `backend`, `frontend`, `proxy`; external network to the legacy and its MySQL; database credentials as Docker `secrets:` from files in `deploy/secrets/` mounted at `/run/secrets/` in `backend` only; R14), `deploy/secrets/README.md` (secret file names and permissions only, no values), `deploy/nginx/default.conf` (routes `/app/` → frontend, `/api/` → backend, `/` → legacy upstream configurable by variable)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: esquema, seguridad, acceso a datos de lectura, soporte de pruebas y shell del frontend, compartidos por todas las historias.

**⚠️ CRITICAL**: no se empieza ninguna historia hasta completar esta fase.

### Esquema (Liquibase, solo aditivo; ver data-model.md)

- [X] T008 Create `backend/src/main/resources/db/changelog/db.changelog-master.yaml` including the changesets below in order, and configure Liquibase in `backend/src/main/resources/application.yml` (enabled at startup for local and test; disabled in the `prod` profile, where it runs only from the runbook T065; never `drop-first`)
- [X] T009 [P] Changeset `backend/src/main/resources/db/changelog/001-guardias-cambios-columnas.yaml`: add nullable columns `TipoGuardiaOrigen varchar(20)`, `FestivoOrigen int`, `TipoGuardiaDestino varchar(20)`, `FestivoDestino int`, `FechaHoraRegistro datetime`, `Canal varchar(20)`, `IdempotencyKey varchar(64)` + unique index `UX_CAMBIO_IDEMPOTENCY (IdempotencyKey)` on `guardias_cambios`
- [X] T010 [P] Changeset `backend/src/main/resources/db/changelog/002-medicos-password-v2.yaml`: add nullable columns `PasswordHashV2 varchar(255)`, `PasswordHashV2Origen varchar(128)`, `LoginFallidosV2 int`, `BloqueadoHastaV2 datetime` on `medicos`
- [X] T011 [P] Changeset `backend/src/main/resources/db/changelog/003-medicos-tipo-historico.yaml` + `backend/src/main/resources/db/changelog/sql/003-trigger-medicos-tipo.sql` (`splitStatements: false`): create `medicos_tipo_historico` (Id PK AI, IdMedico, Tipo, SubTipo, Desde date null, Hasta date null, index `(IdMedico, Desde)`), initial load `INSERT … SELECT ID, Tipo, SubTipo, NULL, NULL FROM medicos WHERE ID IS NOT NULL`, and trigger `AFTER UPDATE ON medicos` that, when `Tipo` or `SubTipo` change, closes the current row (`Hasta = CURDATE()`) and inserts a new one (`Desde = CURDATE()`) (R11)
- [X] T012 [P] Changeset `backend/src/main/resources/db/changelog/004-guardias-cambios-pk.yaml`: precondition `onFail: HALT` asserting `SELECT COUNT(*) FROM (SELECT IdCambio FROM guardias_cambios GROUP BY IdCambio HAVING COUNT(*)>1) d = 0`; then `ADD PRIMARY KEY (IdCambio)` and `MODIFY IdCambio int NOT NULL AUTO_INCREMENT` (R7)
- [X] T013 [P] Changeset `backend/src/main/resources/db/changelog/005-guardias-medicos-unicidad.yaml`: precondition `onFail: HALT` asserting no duplicates on `(IdMedico, LEFT(FGuardia,10), IdServicio)`; then unique index `UX_GUARDIA_MEDICO_DIA (IdMedico, FGuardia(10), IdServicio)` (R8)
- [X] T014 Create the production cleanup kit `deploy/db/limpieza-duplicados.sql` with **only diagnostic SELECTs** that list duplicated `IdCambio` and duplicated guardias (médico, fecha, servicio), plus a commented template of the corrective statements with placeholders; and `deploy/db/README.md` stating it MUST NOT run without a verified backup, review of the concrete IDs and explicit confirmation of the project owner (plan, Approvals §2)

### Soporte de pruebas

- [X] T015 Create `backend/src/test/java/es/medoncalls/guardias/support/MySqlLegacyContainer.java` (Testcontainers `mysql:5.7`, init script `legacy-schema.sql`, then Liquibase update; reusable across test classes) and `backend/src/test/java/es/medoncalls/guardias/support/DatosPrueba.java` (fictitious builders: servicio, médicos adjunto/residente R1–R5/simulado, membership with admin flag, guardias with tipo/festivo, vacaciones)
- [X] T016 [P] Create `backend/src/test/java/es/medoncalls/guardias/legacy/LegacySql.java` with **literal copies** of the legacy SQL used for regression, each with its source reference: calendar query of `src/com/guardias/database/GuardiasDBImpl.java:565-592` (parametrised for test use) and `getTotalCambiosHechosPorTipoMedicoFecha` / `getTotalCambiosRecibidosPorTipoMedicoFecha` of `src/com/guardias/persistence/CambiosGuardias.xml:9-44`
- [X] T017 [P] Create `backend/src/test/java/es/medoncalls/guardias/contract/OpenApiContract.java`: helper that validates MockMvc requests/responses against `contracts/guardias-cambios.openapi.yaml` (swagger-request-validator)

### Transversal backend

- [X] T018 [P] Create `backend/src/main/java/es/medoncalls/guardias/common/ProblemDetailsHandler.java` (`@RestControllerAdvice` returning RFC 7807 for 400/401/403/404/409/422; 422 includes `infracciones`) and `backend/src/main/java/es/medoncalls/guardias/common/ClockConfig.java` (`Clock` bean in `guardias.zona-horaria`; all "today" comparisons use it)
- [X] T019 [P] Create domain primitives in `backend/src/main/java/es/medoncalls/guardias/cambios/domain/`: `TipoGuardia.java` (PRESENCIA, LOCALIZADA, REFUERZO, RESIDENTE ↔ legacy `presencia`/`localizada`/`refuerzo`/`''`), `TipoCambio.java`, `TipoMedico.java` (ADJUNTO/RESIDENTE + subtipo; `esR3aR5()`, `esSimulado()`), `Guardia.java`, `Infraccion.java` (regla, mensaje, medicoId, fecha), `ReglaCodigo.java` (enum of contract codes)
- [X] T020 Create read mappers in `backend/src/main/java/es/medoncalls/guardias/cambios/infrastructure/` with XML in `backend/src/main/resources/mappers/`: `GuardiaMapper` (by servicio and date range; by id; by médico+fecha+servicio; by servicio+fecha; `lockByIds` with `SELECT … FOR UPDATE ORDER BY Id`), `MedicoMapper` (`medicos.Activo`, membership = row exists in `guardias_servicios_medicos` **without** checking `ActivoServicio` (CG-015), nombre; tipo at a date from `medicos_tipo_historico`), `VacacionesMapper` (exists for médico+día). Join médico by `medicos.ID` (not `IdMedico`) (data-model)

### Autenticación y autorización (R2)

- [X] T021 [P] Write unit test `backend/src/test/java/es/medoncalls/guardias/auth/LegacyPasswordVerifierTest.java` with vectors computed with the legacy algorithm (SHA-512 of UTF-8 password, empty salt, Base64; `src/guardias/security/SecurityUtil.java:71-100`), then implement `backend/src/main/java/es/medoncalls/guardias/auth/LegacyPasswordVerifier.java` (constant-time comparison)
- [X] T022 Implement `backend/src/main/java/es/medoncalls/guardias/auth/AuthService.java` + `backend/src/main/java/es/medoncalls/guardias/auth/MedicoAuthMapper.java` + `backend/src/main/resources/mappers/MedicoAuthMapper.xml`: login by email requiring `Activo=1` and `Confirmado=1`; if `PasswordHashV2` exists and `PasswordHashV2Origen` = fingerprint of current `Password`, verify with Argon2id, otherwise verify legacy and store `PasswordHashV2` + fingerprint (SHA-256 of legacy hash); load administered services from `guardias_servicios_medicos.IsAdministrator=1`; lockout per FR-029/R15 (5 consecutive failures → `BloqueadoHastaV2` = now + 15 min; same 401 whether or not the account exists; success resets `LoginFallidosV2`)
- [X] T023 Implement `backend/src/main/java/es/medoncalls/guardias/auth/SecurityConfig.java` (server session, cookie `MOC_SESSION` HttpOnly/Secure/SameSite=Strict path `/api`; CSRF with `CookieCsrfTokenRepository` for the SPA; `/api/v1/auth/login` public, rest authenticated) and `backend/src/main/java/es/medoncalls/guardias/auth/AuthController.java` (`POST /auth/login`, `POST /auth/logout`, `GET /auth/me` per contract)
- [X] T024 Implement `backend/src/main/java/es/medoncalls/guardias/common/ServicioAccess.java`: guard used by every `/servicios/{servicioId}/**` operation, returning 403 unless the session user is administrator of that service (CG-002, FR-001)
- [X] T025 Write integration test `backend/src/test/java/es/medoncalls/guardias/integration/AuthIT.java`: login ok/ko; inactive or unconfirmed user rejected; Argon2 hash stored on first login; after changing `medicos.Password` the V2 hash is discarded and the legacy password is re-verified; lockout after 5 failures, rejected for 15 minutes even with the right password, unlocked afterwards, unknown email gets the same 401 (FR-029); non-admin and admin of another service get 403 on `/servicios/{id}/guardias` (SC-007); contract validation via T017

### Lectura de guardias (compartida por US1 y US2)

- [X] T026 Implement `GET /servicios/{servicioId}/guardias` in `backend/src/main/java/es/medoncalls/guardias/cambios/api/GuardiasController.java` with DTO `backend/src/main/java/es/medoncalls/guardias/cambios/api/dto/GuardiaDto.java`; contract test in `backend/src/test/java/es/medoncalls/guardias/contract/GuardiasContractTest.java`

### Frontend base

- [X] T027 Generate the typed client from `contracts/guardias-cambios.openapi.yaml` into `frontend/src/api/schema.d.ts` (script `gen:api`) and create `frontend/src/api/client.ts` (openapi-fetch, base `/api/v1`, `credentials: 'same-origin'`, sends the CSRF header from the cookie)
- [X] T028 [P] Create `frontend/src/test/server.ts` (MSW handlers derived from the contract examples) and `frontend/src/test/setup.ts`
- [X] T029 Create auth UI: `frontend/src/features/auth/AuthContext.tsx`, `frontend/src/features/auth/LoginPage.tsx`, `frontend/src/features/auth/ServicioSelector.tsx` (for admins of several services) and routing in `frontend/src/App.tsx`; test `frontend/src/features/auth/LoginPage.test.tsx`
- [X] T030 Create the month calendar with guardia selection in `frontend/src/features/cambios/CalendarioPage.tsx` (lists guardias by day with médico, tipo and festivo; selecting a future guardia opens the actions «Ceder» / «Intercambiar»); test `frontend/src/features/cambios/CalendarioPage.test.tsx`

**Checkpoint**: base lista. Login, permisos y lectura del calendario funcionan; las historias pueden empezar.

---

## Phase 3: User Story 1 - Registrar una cesión de guardia (Priority: P1) 🎯 MVP

**Goal**: el administrador cede la guardia de un médico a otro; se aplica al calendario en el acto si cumple las reglas y, si no, se informa de todas las infracciones.

**Independent Test**: con datos ficticios, registrar una cesión válida y comprobar el calendario (también con la SQL del legacy); registrar una cesión por cada regla incumplida y comprobar el 422 y que no cambia nada (spec, historia 1, escenarios 1–8).

### Tests for User Story 1 ⚠️ (escribir primero; deben fallar)

- [X] T031 [P] [US1] Unit tests in `backend/src/test/java/es/medoncalls/guardias/cambios/domain/ReglasCesionTest.java`: CG-005, CG-007 (receptor without other guardia is accepted), CG-008 (today and past rejected), CG-011, CG-012 (vacaciones globales), CG-013 (presencia día anterior/posterior; simulado **no** exento; localizada/refuerzo no afectan), CG-014 (tipo evaluated at guardia date using history), CG-015 (inactive médico rejected; médico not belonging to the service rejected; médico with `ActivoServicio=0` but `Activo=1` and belonging **accepted**), `MISMO_MEDICO`, and several infractions returned together (FR-016)
- [X] T032 [P] [US1] Integration test `backend/src/test/java/es/medoncalls/guardias/integration/RegistrarCesionIT.java`: acceptance scenarios 1–8 of story 1; atomicity (failure injected after updating `guardias_medicos` leaves no change and no `guardias_cambios` row, SC-004); two concurrent cessions of the same guardia → exactly one 201 (SC-004, SC-005); repeated `Idempotency-Key` applies once and returns the same body; repeating without key → 422 CG-005; 403 for non-admin; receptor from another service → 422 CG-015 (edge case «médico de otro servicio»); with `ACTIVAR_CAMBIO_GUARDIAS='N'` for the service the API still registers the cession (FR-004)
- [X] T033 [P] [US1] Contract test `backend/src/test/java/es/medoncalls/guardias/contract/CambiosContractTest.java`: `POST /cambios` CESION 201 and 422 (`ReglasIncumplidas` with all infracciones), 400 when `guardiaDestinoId` is sent in CESION; `GET …/receptores-candidatos?tipo=CESION`
- [X] T034 [P] [US1] Legacy visibility test `backend/src/test/java/es/medoncalls/guardias/legacy/LegacyVisibilidadIT.java`: after a cession, the legacy calendar SQL (`LegacySql`) returns the receptor on that date with the same tipo/festivo and not the cedente (SC-008)

### Implementation for User Story 1

- [X] T035 [US1] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/domain/SolicitudCambio.java` (tipo, guardiaOrigenId, cedenteId, receptorId, guardiaDestinoId opcional) and `backend/src/main/java/es/medoncalls/guardias/cambios/domain/ContextoValidacion.java` (snapshot loaded inside the transaction: guardias afectadas, guardias del día de cedente/receptor y de los días adyacentes, vacaciones, tipos en fecha, estado activo/miembro, hoy)
- [X] T036 [US1] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/domain/ReglaPresenciasConsecutivas.java` (FR-012: generic for any médico that receives a presencia; ignores the adjacent day equal to the other date of the same change; no simulado exemption)
- [X] T037 [US1] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/domain/ReglasCambio.java` returning `List<Infraccion>` for the common rules and the CESION case (CG-005, CG-007, CG-008, CG-011, CG-012, CG-013 via T036, CG-014, CG-015, MISMO_MEDICO); messages from `backend/src/main/resources/messages_es.properties` (UTF-8) naming médico and fecha (SC-003)
- [X] T038 [US1] Implement write mapper `backend/src/main/java/es/medoncalls/guardias/cambios/infrastructure/CambioMapper.java` + `backend/src/main/resources/mappers/CambioMapper.xml` (insert into `guardias_cambios` per data-model: `Estado='APROBADA'`, `Canal='APP'`, `FechaFinCambio = FechaIniCambio` for CESION (R10), `TipoGuardiaOrigen/FestivoOrigen` from the pre-change snapshot, `UsuarioAprobacion` = admin, `FechaHoraRegistro`; find by `IdempotencyKey`) and add `updateMedico(guardiaId, medicoId)` to `GuardiaMapper`
- [X] T039 [US1] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/application/RegistrarCambioService.java`: `@Transactional`; idempotency lookup; `lockByIds` of the affected guardias in `Id` order; build `ContextoValidacion`; evaluate `ReglasCambio`; on infractions throw `ReglasIncumplidasException` (`backend/src/main/java/es/medoncalls/guardias/cambios/application/ReglasIncumplidasException.java`); apply the CESION effect (FR-019) and insert the record; map unique-index violations to 422 CG-011
- [X] T040 [US1] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/application/CandidatosService.java` for CESION (médicos with `Activo=1` belonging to the service — no `ActivoServicio` filter —, same tipo at the date, without guardia or vacaciones that day; FR-017) and `GET /servicios/{servicioId}/guardias/{guardiaId}/receptores-candidatos` in `backend/src/main/java/es/medoncalls/guardias/cambios/api/CambiosController.java`
- [X] T041 [US1] Implement `POST /servicios/{servicioId}/cambios` in `backend/src/main/java/es/medoncalls/guardias/cambios/api/CambiosController.java` with DTOs `backend/src/main/java/es/medoncalls/guardias/cambios/api/dto/RegistrarCambioRequest.java` and `backend/src/main/java/es/medoncalls/guardias/cambios/api/dto/CambioDto.java`; enforce `ServicioAccess`; the admin is taken from the session, never from the body
- [X] T042 [P] [US1] Frontend test `frontend/src/features/cambios/CesionForm.test.tsx` (MSW): candidates shown, success refreshes the calendar, 422 lists every infraction with médico and fecha
- [X] T043 [US1] Implement `frontend/src/features/cambios/CesionForm.tsx` and connect it from «Ceder» in `frontend/src/features/cambios/CalendarioPage.tsx` (Idempotency-Key per form submission; disable button while sending)

**Checkpoint**: la cesión funciona de extremo a extremo y se puede desplegar como MVP.

---

## Phase 4: User Story 2 - Registrar un intercambio de guardias (Priority: P1)

**Goal**: el administrador intercambia las guardias de dos médicos en dos fechas; tipo y festivo se quedan con cada día.

**Independent Test**: registrar un intercambio válido y comprobar ambos días; registrar intercambios que incumplen cada regla y comprobar el 422 (spec, historia 2, escenarios 1–5).

### Tests for User Story 2 ⚠️

- [ ] T044 [P] [US2] Unit tests in `backend/src/test/java/es/medoncalls/guardias/cambios/domain/ReglasIntercambioTest.java`: CG-006, CG-009 (destino hoy/pasado), CG-011 and CG-012 for both médicos, CG-013 for both including the adjacent-day exception (escenario 5), CG-014 at both dates
- [ ] T045 [P] [US2] Integration test `backend/src/test/java/es/medoncalls/guardias/integration/RegistrarIntercambioIT.java`: escenarios 1–5 of story 2 (tipo y festivo permanecen con el día); concurrent intercambio and cesión over the same guardia → only one succeeds
- [ ] T046 [P] [US2] Extend `backend/src/test/java/es/medoncalls/guardias/contract/CambiosContractTest.java`: CAMBIO 201, 400 without `guardiaDestinoId`, `receptores-candidatos?tipo=CAMBIO&fechaDestino=` returns `guardiaDestinoId`

### Implementation for User Story 2

- [ ] T047 [US2] Extend `backend/src/main/java/es/medoncalls/guardias/cambios/domain/ReglasCambio.java` with the CAMBIO rules evaluated for both médicos (CG-006, CG-009, CG-011, CG-012, CG-013, CG-014)
- [ ] T048 [US2] Extend `backend/src/main/java/es/medoncalls/guardias/cambios/application/RegistrarCambioService.java` and `backend/src/main/resources/mappers/CambioMapper.xml` for CAMBIO: lock both guardias, swap `IdMedico` (FR-018), store `FechaFinCambio` = fecha destino and `TipoGuardiaDestino/FestivoDestino` from the snapshot
- [ ] T049 [US2] Extend `backend/src/main/java/es/medoncalls/guardias/cambios/application/CandidatosService.java` for CAMBIO with `fechaDestino` (médicos with guardia that day, same tipo, returning `guardiaDestinoId`)
- [ ] T050 [P] [US2] Frontend test `frontend/src/features/cambios/IntercambioForm.test.tsx` (MSW)
- [ ] T051 [US2] Implement `frontend/src/features/cambios/IntercambioForm.tsx` (choose fecha destino and the candidate's guardia) and connect it from «Intercambiar» in `frontend/src/features/cambios/CalendarioPage.tsx`

**Checkpoint**: cesión e intercambio funcionan de forma independiente.

---

## Phase 5: User Story 3 - Ajuste automático de las guardias de adjunto por residentes R3–R5 (Priority: P2)

**Goal**: si tras un cambio un día pasa a tener o deja de tener un residente R3–R5, las guardias de adjunto de ese día cambian entre refuerzo y localizada.

**Independent Test**: con residentes R2/R3/R4 ficticios, registrar cesiones e intercambios que añadan o quiten un R3–R5 de un día y comprobar el tipo de las guardias de adjunto (spec, historia 3).

### Tests for User Story 3 ⚠️

- [ ] T052 [P] [US3] Unit tests `backend/src/test/java/es/medoncalls/guardias/cambios/domain/AjusteResidentesTest.java`: entra R3–R5 → refuerzo→localizada; sale → localizada→refuerzo; ambos R3–R5 o ninguno → sin cambios; residente receptor (no solicitante) también ajusta; recuento por día
- [ ] T053 [P] [US3] Integration test `backend/src/test/java/es/medoncalls/guardias/integration/AjusteResidentesIT.java`: escenarios 1–4 for both CESION and CAMBIO (two affected dates), all within the same transaction (rollback test)

### Implementation for User Story 3

- [ ] T054 [US3] Implement `backend/src/main/java/es/medoncalls/guardias/cambios/domain/AjusteResidentes.java`: for each affected date compare the number of residents with subtipo R3–R5 (tipo at that date) before and after the change and return the adjustment (FR-020; data-model «Efectos» §3)
- [ ] T055 [US3] Add `updateTipoPorDia(servicioId, fecha, tipoDesde, tipoHasta)` to `backend/src/main/java/es/medoncalls/guardias/cambios/infrastructure/GuardiaMapper.java` / `backend/src/main/resources/mappers/GuardiaMapper.xml` and apply `AjusteResidentes` inside `backend/src/main/java/es/medoncalls/guardias/cambios/application/RegistrarCambioService.java` (lock those guardias too)

**Checkpoint**: el calendario resultante es coherente con el nivel de los residentes.

---

## Phase 6: User Story 4 - Datos de las cesiones para el equilibrio del reparto (Priority: P2)

**Goal**: cada cesión guarda el tipo y el festivo de la guardia cedida; el cálculo de equilibrio del legacy no cambia de comportamiento.

**Independent Test**: registrar cesiones de distintos tipos y comprobar lo guardado; ejecutar las consultas de equilibrio del legacy sobre un conjunto fijo antes y después de las migraciones (spec, historia 4).

### Tests for User Story 4 ⚠️

- [ ] T056 [P] [US4] Integration test `backend/src/test/java/es/medoncalls/guardias/integration/DatosEquilibrioIT.java`: CESION stores `TipoGuardiaOrigen/FestivoOrigen` of the ceded guardia even if the receptor has other guardias of another tipo; CAMBIO stores both origin and destination; API `CambioDto.guardiaOrigen/guardiaDestino` match (FR-003, FR-023)
- [ ] T057 [P] [US4] Legacy regression `backend/src/test/java/es/medoncalls/guardias/legacy/LegacyEquilibrioIT.java`: with a fixed fictitious dataset, the two legacy equity queries return identical results before and after the Liquibase changesets (SC-006); a cession registered through the API is counted by the «hechas» query for the cedente with the ceded tipo/festivo (R10)

### Implementation for User Story 4

- [ ] T058 [US4] Verify and complete the snapshot mapping in `backend/src/main/resources/mappers/CambioMapper.xml` and `backend/src/main/java/es/medoncalls/guardias/cambios/api/dto/CambioDto.java` so that T056 and T057 pass (values captured **before** applying the effect)

**Checkpoint**: todas las historias funcionan de forma independiente.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: listado y corrección, despliegue, convivencia con el legacy y validación final.

- [ ] T059 Implement `GET /servicios/{servicioId}/cambios` in `backend/src/main/java/es/medoncalls/guardias/cambios/api/CambiosController.java` (+ query in `CambioMapper.xml`; `fechaDestino` and `guardiaDestino` omitted for CESION even though the legacy column stores `FechaFinCambio = FechaIniCambio` (R10); contract test in `backend/src/test/java/es/medoncalls/guardias/contract/CambiosContractTest.java`)
- [ ] T060 Implement `frontend/src/features/cambios/CambiosListPage.tsx` with action «Registrar cambio inverso» that pre-fills the cesión/intercambio form with cedente and receptor swapped (FR-028); test `frontend/src/features/cambios/CambiosListPage.test.tsx`
- [ ] T061 [P] Create `backend/Dockerfile` (multi-stage, JRE 21, non-root user) and `frontend/Dockerfile` (build + nginx serving `/app/`); complete `deploy/docker-compose.yml` and `deploy/nginx/default.conf`; document local run in `deploy/README.md`
- [ ] T062 [P] Security review: no secrets in the repository (`git grep` for passwords/keys), cookies and CSRF verified in `backend/src/test/java/es/medoncalls/guardias/integration/AuthIT.java`, all `/servicios/**` endpoints covered by `ServicioAccess` (test listing controller mappings in `backend/src/test/java/es/medoncalls/guardias/integration/ServicioAccessCoverageTest.java`)
- [ ] T063 Verify in a test environment with **MySQL 5.7** (same version as production, not MariaDB) and fictitious data that the legacy agenda generator (`web/calc_agenda.jsp`, `web/fill_agenda.jsp`) does not insert duplicated guardias once `UX_GUARDIA_MEDICO_DIA` exists (R8); record procedure and result in `specs/001-registro-cambios-guardia/validacion.md`
- [ ] T064 Verify in the **MySQL 5.7** test environment that the trigger of T011 can be created with the MySQL user and binlog configuration intended for production (R11); record result in `specs/001-registro-cambios-guardia/validacion.md`
- [ ] T065 Write the rollout runbook `deploy/RUNBOOK.md`: (1) verified backup; (2) diagnostics with `deploy/db/limpieza-duplicados.sql` and review of concrete IDs with the owner; (3) **explicit confirmation** before any corrective statement; (4) Liquibase update launched explicitly (CLI or Maven plugin with production secrets; auto-run is disabled in `prod`, R6); (5) `UPDATE configuracion SET config_value='N' WHERE config_key='ACTIVAR_CAMBIO_GUARDIAS' AND config_IdServicio=?` (R13); (6) smoke test from quickstart §2; (7) rollback steps
- [ ] T066 Add the approved link to `/app/` in `web/common/navigation.jsp` (single line, visible to administrators; **preserve the file encoding** — check it before editing and use an encoding-preserving edit) — only legacy change of this feature (plan, Approvals §1)
- [ ] T067 Run `mvn -f backend/pom.xml verify` and `npm --prefix frontend test`, then the manual scenario of `specs/001-registro-cambios-guardia/quickstart.md` §2; record commands, results and deviations in `specs/001-registro-cambios-guardia/validacion.md` (principle V: no test is reported as passed unless executed)
- [ ] T068 Run `/migration-review` on the increment against spec, rules catalog `docs/rules/cambio-guardia-reglas-candidatas.md`, `contracts/guardias-cambios.openapi.yaml` and legacy behaviour; fix findings before opening the PR

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias.
- **Foundational (Phase 2)**: depende de Setup y **bloquea todas las historias**.
- **US1 (Phase 3)**: depende de Foundational. Es el MVP.
- **US2 (Phase 4)**: depende de Foundational y reutiliza `ReglasCambio`, `ReglaPresenciasConsecutivas`, `RegistrarCambioService` y `CambioMapper` creados en US1 (T035–T039). Se prueba de forma independiente.
- **US3 (Phase 5)**: depende de que exista al menos US1 (aplica dentro de `RegistrarCambioService`); se prueba con cesiones y, si US2 está hecha, también con intercambios.
- **US4 (Phase 6)**: depende de US1 (registro de cesiones); T058 ajusta lo que crean T038 y T048.
- **Polish (Phase 7)**: T059–T060 dependen de US1; T063–T064 dependen de T011–T013; T065 de T014; T067–T068 de todo lo demás.

### Within Each User Story

- Las pruebas se escriben primero y deben fallar.
- Dominio → infraestructura → servicio de aplicación → API → frontend.
- La historia se termina antes de pasar a la siguiente prioridad.

### Parallel Opportunities

- Setup: T002–T007 en paralelo tras T001.
- Foundational: T009–T013 en paralelo (ficheros distintos; T008 los incluye); T016–T019 en paralelo; T021 en paralelo con T020; T028 en paralelo con T027.
- En cada historia, las pruebas marcadas [P] en paralelo.
- Frontend y backend de una misma historia en paralelo cuando el contrato está publicado (T003): p. ej. T042–T043 junto a T035–T041.

---

## Parallel Example: User Story 1

```text
# Pruebas de US1 a la vez:
Task: "T031 Unit tests ReglasCesionTest.java"
Task: "T032 Integration test RegistrarCesionIT.java"
Task: "T033 Contract test CambiosContractTest.java"
Task: "T034 Legacy visibility LegacyVisibilidadIT.java"

# Frontend en paralelo con el backend (contrato ya publicado):
Task: "T042 CesionForm.test.tsx"  →  "T043 CesionForm.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 (Setup) + Phase 2 (Foundational).
2. Phase 3 (US1, cesión).
3. **Parar y validar**: pruebas de US1 y quickstart §2 pasos 1–4 y 8.
4. Desplegar en pruebas o hacer la demostración. La cesión cubre el 65 % de los cambios históricos.

### Incremental Delivery

1. Setup + Foundational → base lista.
2. US1 → validar → MVP.
3. US2 → validar (intercambios).
4. US3 + US4 → validar (ajuste de residentes, datos del equilibrio y regresión legacy).
5. Polish → runbook, enlace en el legacy, validación completa, `/migration-review` → PR.
6. **Puesta en producción** solo con T065 y la confirmación explícita de la limpieza de datos.

---

## Notes

- `[P]` = ficheros distintos y sin dependencias pendientes.
- La etiqueta `[USx]` enlaza cada tarea con su historia para la trazabilidad.
- Hacer commit tras cada tarea o grupo lógico.
- Ninguna tarea ejecuta operaciones sobre la base de datos de producción; T014 y T065 solo preparan lo necesario para una ejecución confirmada.
