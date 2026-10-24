# Implementation Plan: Registro de cambios y cesiones de guardia

**Branch**: `001-registro-cambios-guardia` (trabajo actual en `rearchitecture-claude-based-speckit`) | **Date**: 2026-10-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-registro-cambios-guardia/spec.md`

**Note**: primer plan del proyecto. Fija la ubicación del código nuevo, la infraestructura y la convivencia con el esquema MySQL del legacy (constitución v1.1.0, restricciones técnicas).

## Summary

Un administrador de servicio registra cambios (intercambios) y cesiones de guardia desde una aplicación React nueva servida en `/app/`. Un backend Spring Boot valida en el servidor todas las reglas validadas por negocio (CG-002 a CG-021) y aplica el cambio al calendario en una única transacción con bloqueo de filas. Usa directamente las tablas del legacy, para que el legacy vea los cambios al instante, más unos pocos cambios de esquema aditivos:
- columnas nuevas en `guardias_cambios` (tipo y festivo previos, canal, idempotencia);
- histórico de tipo de médico alimentado por un trigger;
- columnas de hash adaptativo para el login;
- índice único de guardias por médico, día y servicio.

La pantalla de cambios del legacy se desactiva por configuración, sin tocar su código. Detalle de las decisiones en [research.md](./research.md).

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript 5 en modo estricto (frontend).

**Primary Dependencies**: Spring Boot 3.x (Web, Security, Validation), MyBatis Spring Boot Starter 3.x, Liquibase, driver JDBC de MySQL; React 18, Vite, React Router y un cliente HTTP tipado generado a partir del contrato OpenAPI.

**Storage**: la MySQL 5.7 existente del legacy (esquema compartido, cambios solo aditivos; [data-model.md](./data-model.md)).

**Testing**: JUnit 5, Spring Boot Test, Testcontainers (MySQL 5.7) y validación contra OpenAPI; Vitest, Testing Library y MSW. Regresión legacy ejecutando su SQL original (R12).

**Target Platform**: contenedores Linux (Docker) en la misma red que el legacy, detrás de un proxy inverso nginx en el mismo dominio (R1).

**Project Type**: aplicación web (backend API + frontend SPA) en convivencia con un monolito JSP legacy.

**Performance Goals**: volumen bajo (unos 600–700 cambios al año y 2 administradores por servicio). Objetivo de usuario: registrar un cambio en menos de 1 minuto (SC-001); respuesta del alta en menos de 1 s en condiciones normales.

**Constraints**: legacy en solo lectura salvo lo aprobado (principio III); compatibilidad hacia atrás del esquema mientras convivan; transacción atómica por cambio (FR-021); sin datos de producción en pruebas (principio VI); MySQL 5.7 (fuera de soporte; su actualización queda fuera de alcance).

**Scale/Scope**: 1 servicio principal (unas 10.750 guardias históricas), unos 40 médicos; 3 pantallas (login, calendario con selección, alta de cambio y listado).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio / norma | Cumplimiento en este plan | Estado |
|---|---|---|
| I. Reglas validadas | Cada validación y efecto viene de una regla `validada` del catálogo (data-model, «Reglas de validación»). CG-016 (pendiente) no se implementa como regla. | ✅ |
| II. Reglas en el servidor | Todas las reglas y el rol se comprueban en el backend; los candidatos de la interfaz son solo una ayuda (FR-017). Identidad tomada de la sesión, nunca del cuerpo de la petición. | ✅ |
| III. Migración incremental | Incremento acotado a cambios y cesiones; legacy en solo lectura. **Excepción**: enlace en `navigation.jsp` (ver Complexity Tracking). | ✅ excepción justificada y aprobada (2026-10-24) |
| IV. Contrato primero | Borrador en [contracts/](./contracts/guardias-cambios.openapi.yaml); se publica en `contracts/guardias-cambios.openapi.yaml` antes de implementar los endpoints. Errores con Problem Details. | ✅ |
| V. Evidencia y pruebas | Las decisiones citan evidencia legacy; [quickstart.md](./quickstart.md) define pruebas por escenario y la regresión legacy. | ✅ |
| VI. Datos y secretos | Pruebas con DDL y datos ficticios; hash adaptativo en el login nuevo; secretos (credenciales de la base) como **secretos de Docker** montados en `/run/secrets`, leídos por Spring con `configtree`, nunca en el repositorio ni en variables de entorno planas (R14); bloqueo de login por intentos fallidos (FR-029). El SHA-512 legacy queda registrado como excepción en Complexity Tracking. | ✅ con excepción registrada |
| VII. Integridad de datos | Transacción y `FOR UPDATE` (R9); PK y AUTO_INCREMENT en `guardias_cambios` (R7); SQL parametrizada (MyBatis `#{}`); índice único de guardias (R8); `Idempotency-Key`. Limpieza de datos previa a R7 y R8 aprobada en el plan (2026-10-24); su ejecución se confirma en el momento. | ✅ aprobado |
| Restricciones técnicas | Spring Boot 3.x y Java 21; React y TypeScript; contratos en `contracts/`; infraestructura como código en `deploy/` (decidida aquí); esquema legacy como fuente de verdad. | ✅ |
| Flujo (punto 2) | La spec no tiene preguntas abiertas sobre reglas críticas (CG-016 no es crítica y queda fuera). | ✅ |
| Flujo (punto 6) | Sin operaciones destructivas sin autorización: la limpieza de duplicados y los `ALTER` de producción son tareas con aprobación explícita. | ✅ |

**Resultado de la puerta (antes de la fase 0)**: superada; las excepciones de Complexity Tracking están aprobadas (ver Approvals).

**Re-check tras la fase 1**: el diseño (data-model, contrato, quickstart) no introduce nuevas desviaciones.

## Project Structure

### Documentation (this feature)

```text
specs/001-registro-cambios-guardia/
├── plan.md              # Este fichero
├── research.md          # Fase 0: decisiones R1–R13
├── data-model.md        # Fase 1: entidades, correspondencia con el esquema legacy y reglas
├── quickstart.md        # Fase 1: validación de extremo a extremo
├── contracts/
│   └── guardias-cambios.openapi.yaml   # Borrador del contrato
├── checklists/
│   └── requirements.md
└── tasks.md             # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
contracts/
└── guardias-cambios.openapi.yaml        # Contrato publicado (principio IV)

backend/                                 # Spring Boot 3.x, Java 21, Maven
├── pom.xml
└── src/
    ├── main/java/es/medoncalls/guardias/
    │   ├── auth/                         # login sobre medicos, hash adaptativo, sesión, CSRF
    │   ├── cambios/
    │   │   ├── api/                      # controladores y DTO según el contrato
    │   │   ├── domain/                   # reglas CG-xxx y efectos (sin dependencias de framework)
    │   │   ├── application/              # caso de uso «registrar cambio» (transacción, bloqueo, idempotencia)
    │   │   └── infrastructure/           # mappers MyBatis de las tablas legacy
    │   └── common/                       # Problem Details, reloj y zona horaria, seguridad por servicio
    ├── main/resources/
    │   ├── mappers/                      # SQL MyBatis
    │   └── db/changelog/                 # Liquibase (solo aditivo)
    └── test/
        ├── java/…/unit/                  # reglas del dominio
        ├── java/…/integration/           # Testcontainers MySQL 5.7: transacción, concurrencia, permisos
        ├── java/…/contract/              # validación contra contracts/
        ├── java/…/legacy/                # regresión con la SQL legacy (SC-006, SC-008)
        └── resources/legacy-schema.sql   # DDL extraído del dump, sin datos

frontend/                                # Vite, React 18, TypeScript estricto
├── package.json
└── src/
    ├── api/                              # cliente generado desde contracts/
    ├── features/auth/
    ├── features/cambios/                 # calendario, selección de guardia, alta, listado
    └── test/                             # Vitest, Testing Library, MSW

deploy/
├── docker-compose.yml                    # backend, frontend (nginx estático) y proxy
└── nginx/                                # rutas /app/ → frontend, /api/ → backend, / → legacy

src/, web/, resources/, build/            # Legacy: sin cambios salvo la excepción aprobada
```

**Structure Decision**: aplicación web con `backend/` y `frontend/` en la raíz, contrato publicado en `contracts/` y despliegue en `deploy/`. El legacy no se mueve. En el backend se separa el dominio (reglas puras, fáciles de probar contra el catálogo) de la infraestructura (SQL del esquema legacy).

## Complexity Tracking

Las fechas de revisión son una propuesta del plan, ajustable por el responsable del proyecto (constitución, Gobierno: toda excepción con alcance y fecha de revisión).

| Violation | Why Needed | Simpler Alternative Rejected Because | Alcance y revisión |
|-----------|------------|-------------------------------------|--------------------|
| Modificar el legacy: un enlace a `/app/` en `web/common/navigation.jsp` (principio III) | Que el administrador encuentre la pantalla nueva desde el legacy durante la convivencia | Sin enlace (URL o marcador): factible y sin tocar el legacy, pero peor para el usuario. Aprobada la modificación (2026-10-24). | Una línea en `navigation.jsp`, solo en esta funcionalidad. Revisión: al retirar el calendario legacy o el 2027-04-30, lo que ocurra antes |
| Limpieza de datos en producción: 3 `IdCambio` duplicados y 3 guardias duplicadas, y después `ALTER` para la PK y el índice único (flujo 6, principio VII) | Sin limpieza no se pueden crear la PK y el índice que exige el principio VII | Generar IDs con una tabla de secuencia y validar la unicidad solo en código: deja la invariante sin garantía en base de datos y dos mecanismos de ID conviviendo | Una ejecución en la puesta en marcha (T065), sobre los IDs revisados. Revisión: tras la ejecución, verificando los índices creados |
| Trigger en la tabla legacy `medicos` (cambio de esquema aditivo) | Registrar en el histórico los cambios de tipo hechos desde el legacy sin modificar su código (FR-024) | Modificar el código del legacy (viola el principio III); no tener histórico (incumple CG-014) | Trigger sobre `medicos` mientras el legacy edite médicos. Revisión: al migrar la gestión de médicos o el 2027-04-30 |
| Principio VI: el hash SHA-512 sin sal del legacy sigue en `medicos.Password` | El login del legacy lo necesita mientras conviva; el sistema nuevo guarda además un hash adaptativo (R2) | Forzar el cambio de contraseña o migrar el login del legacy: fuera del alcance de esta funcionalidad y rompe el acceso al legacy | Solo la columna legacy; el login nuevo nunca guarda SHA-512. Revisión: al retirar el login del legacy o el 2027-04-30 |

## Approvals

Aprobado por David (Administrador, responsable del proyecto) el 2026-10-24:

1. **Enlace en `navigation.jsp`**: aprobado como única modificación del código legacy en esta funcionalidad.
2. **Limpieza de duplicados y `ALTER` en producción**: aprobados en el plan. La **ejecución** en producción sigue requiriendo, en su momento, una copia de seguridad verificada, la revisión del script con los IDs concretos y una confirmación explícita antes de lanzarlo.
3. **Trigger sobre `medicos`** para el histórico de tipos: aprobado.

Pendiente operativo: fijar `ACTIVAR_CAMBIO_GUARDIAS = 'N'` en el servicio al activar la aplicación nueva (R13), dentro de la ventana de puesta en marcha.
