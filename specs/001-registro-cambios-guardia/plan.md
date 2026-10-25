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

**Primary Dependencies**: Spring Boot 3.x (Web, Security, Validation), MyBatis Spring Boot Starter 3.x, Liquibase, driver JDBC de MySQL; React 18, Vite, React Router y un cliente HTTP tipado generado a partir del contrato OpenAPI. FullCalendar 6.1.21 (`@fullcalendar/react`, `core`, `daygrid`, `multimonth`; MIT, versión exacta) para las vistas de año, mes, semana y día del calendario (FR-030): es la evolución del FullCalendar 3 del legacy y lo recomienda CLAUDE.md frente a construir las vistas a mano. `@fullcalendar/interaction` (misma versión) para arrastrar y soltar guardias (FR-032, H2).

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
| VIII. Capacidades de la interfaz | Inventario de las pantallas legacy afectadas y su cobertura en [Capacidades de la interfaz legacy](#capacidades-de-la-interfaz-legacy-principio-viii). Huecos decididos el 2026-10-25: H1, H2 y H3 se migran (FR-030, FR-032, FR-031); H4 y H5 se aceptan y constan en la spec. | ✅ decidido (2026-10-25) |

**Resultado de la puerta (antes de la fase 0)**: superada; las excepciones de Complexity Tracking están aprobadas (ver Approvals).

**Re-check tras la fase 1**: el diseño (data-model, contrato, quickstart) no introduce nuevas desviaciones.

**Re-check con la constitución 1.2.0** (2026-10-25): el principio VIII se añadió con la implementación ya hecha. Se hizo el inventario de abajo y el responsable decidió los huecos H1–H5 el mismo día.

## Capacidades de la interfaz legacy (principio VIII)

**Alcance de la migración de interfaz.** Esta funcionalidad no migra el calendario legacy (`web/inicio.jsp`): sigue en uso para consultar guardias, generar la agenda, exportar y sincronizar con Google Calendar. Lo que pasa a la aplicación nueva es **solo el registro de cambios**. Al desactivar `ACTIVAR_CAMBIO_GUARDIAS` (R13, runbook paso 5), el legacy deja de permitir cambios y las capacidades de cambio de las tres pantallas de abajo pasan a cubrirse en `/app/`.

Estados: **cubierta** (misma capacidad), **sustituida** (equivalente con otro widget, permitido por el principio VIII), **descartada por spec** (decisión ya aprobada), **sigue en legacy** (fuera del alcance, sin pérdida), **hueco** (se pierde sin decisión aprobada; ver H1–H5).

### Calendario legacy: cambio desde el calendario (`web/inicio.jsp`)

| # | Capacidad legacy | Evidencia | En la aplicación nueva | Estado |
|---|---|---|---|---|
| C1 | Vistas año, mes, semana y día; navegación anterior/siguiente/hoy | `web/inicio.jsp:294-297` | Las mismas vistas con FullCalendar 6 (`multiMonthYear`, `dayGridMonth`, `dayGridWeek`, `dayGridDay`), «Hoy» y anterior/siguiente (`CalendarioPage.tsx`, FR-030) | cubierta (H1) |
| C2 | Saltar a la vista de un día o semana pulsando su nombre (`navLinks`) | `web/inicio.jsp:302` | `navLinks` de FullCalendar: el número de cada día lleva a su vista de día (FR-030) | cubierta (H1) |
| C3 | Enlace «más» cuando un día tiene muchas guardias (`eventLimit`) | `web/inicio.jsp:307` | `dayMaxEvents`: enlace «+N más» con todas las guardias del día; la vista de día las muestra todas (FR-030) | cubierta |
| C4 | Mover una guardia a otra fecha arrastrándola dentro del calendario, con confirmación «¿Estás seguro…?» | `web/inicio.jsp:306`, `394-396`, `105`, `640` | Arrastrar una guardia futura a otro día futuro abre el popup «Cambio de guardia» con Inicio, Fin y los candidatos de ese día; se confirma con «Registrar intercambio». Alternativa sin arrastrar: pulsar o teclado → popup (`CalendarioPage.tsx`, `Dialogo.tsx`, FR-032) | cubierta (H2) |
| C5 | Bandeja externa: arrastrar una guardia fuera del calendario, cambiar de mes y soltarla en otra fecha | `web/inicio.jsp:304`, `348-355`, `575-576` | Arrastrar entre meses en la vista de año, o editar la fecha de Fin en el popup (cualquier mes) | sustituida (H2) |
| C6 | Prevalidación al soltar, en el servidor (`cambio_guardia.jsp`) | `web/inicio.jsp:163` | Candidatos ya filtrados (FR-017) y validación completa en el servidor con todas las reglas incumplidas (FR-016) | cubierta (mejorada) |
| C7 | Leyenda de colores por tipo: Presencia, Localizada, Refuerzo, Residente; festivos marcados (`festivoc`) | `web/inicio.jsp:450-453`, `web/get_guardias.jsp:111`, `web/css/custom.css:55-68,137` | Colores por tipo con tokens CSS y contraste AA, trama para festivos y leyenda (`calendario.css`). El título incluye tipo y «(festivo)», así que el color no es la única pista (FR-031) | cubierta (H3) |
| C8 | Guardias ordenadas dentro de cada día: presencia, residente, y después localizada y refuerzo | `src/com/guardias/database/GuardiasDBImpl.java:565,592` (`ORDER BY Sorted DESC`); `web/js/guardias.js:68-80` y `web/get_guardias.jsp:129` conservan ese orden | Mismo orden con `eventOrder` y, a igual tipo, por `Id` de guardia (`fechas.ts`, `ordenTipo`; FR-031) | cubierta (H3) |
| C9 | Un médico no administrador solo puede arrastrar sus propias guardias | `web/inicio.jsp:387` (`_IsMedicoONCall`, solo en el cliente) | Solo el administrador del servicio registra cambios, comprobado en el servidor (FR-001) | descartada por spec (solicitud del médico fuera de alcance; FR-001/CG-002) |
| C10 | Generar agenda, exportar a Excel, Google Calendar | `web/inicio.jsp` (otras secciones) | No forman parte de esta funcionalidad | sigue en legacy |

### Formulario de cambio legacy (`web/medico/_list_medicos_cambio_guardia.jsp`)

| # | Capacidad legacy | Evidencia | En la aplicación nueva | Estado |
|---|---|---|---|---|
| F1 | Fechas de origen y destino visibles y no editables | `_list_medicos_cambio_guardia.jsp:93-95` | Cesión: fecha y tipo de la guardia en el formulario. Intercambio: fecha de origen fija y fecha de destino elegible (`IntercambioForm.tsx:86`) | cubierta |
| F2 | El administrador elige el médico solicitante entre los que tienen guardia ese día | `_list_medicos_cambio_guardia.jsp:103` | Se elige directamente la guardia (con su médico) en el calendario | sustituida |
| F3 | Elegir el tipo: CAMBIO o CESION | `_list_medicos_cambio_guardia.jsp:129-132` | Dos acciones, «Ceder» e «Intercambiar» | sustituida |
| F4 | Elegir quién recibe, con la lista limitada a médicos del mismo tipo | `_list_medicos_cambio_guardia.jsp:135-140`, `186-194` (filtro solo en el cliente) | Lista de candidatos calculada en el servidor con todas las reglas (FR-013, FR-017) | cubierta (mejorada) |
| F5 | Guardar | `_list_medicos_cambio_guardia.jsp:165` | «Registrar» con `Idempotency-Key` (`useRegistrarCambio.ts`) | cubierta |
| F6 | El cambio queda pendiente de aprobación | flujo de estados del legacy | Se aplica en el acto | descartada por spec (FR-002/CG-003) |
| F7 | Correo al registrar | flujo legacy | Sin notificaciones | descartada por spec (FR-027/CG-022) |

### Listado de cambios legacy (`web/medico/lista_cambiosguardias.jsp`, en `web/cambios_guardias.jsp`)

| # | Capacidad legacy | Evidencia | En la aplicación nueva | Estado |
|---|---|---|---|---|
| L1 | Tabla con columnas De, A, Tipo, Destinatario, Solicitud, Aprobación y Estado | `lista_cambiosguardias.jsp:27-38` | Lista (no tabla) de los cambios del mes: descripción con fechas, tipo y médicos, más quién lo registró y cuándo (`CambiosListPage.tsx:128-150`). Sin Aprobación ni Estado, que no existen (FR-002) | cubierta (columnas de aprobación descartadas por FR-002) |
| L2 | Búsqueda, ordenación por columna y paginación (DataTables) | `lista_cambiosguardias.jsp:16` | Lista por mes, ordenada del más reciente al más antiguo, con navegación entre meses; sin búsqueda ni ordenación por columna | **hueco H4** |
| L3 | Tabla adaptable al móvil (`responsive`) | `lista_cambiosguardias.jsp:16`, `web/cambios_guardias.jsp:23,38` | Sin comprobar en pantallas estrechas | a verificar en T068 (**H4**) |
| L4 | El administrador ve todos los cambios del servicio; un médico ve los suyos | `lista_cambiosguardias.jsp:44-54` | El administrador ve los de su servicio. Los médicos no tienen acceso a la aplicación nueva | administrador: cubierta. Médico: **hueco H5** |
| L5 | Aprobar o cancelar un cambio pendiente | `lista_cambiosguardias.jsp:94-99` | No hay estados. Un error se corrige registrando el cambio inverso («Registrar cambio inverso», `CambiosListPage.tsx:143`) | descartada por spec (FR-002) y sustituida por el cambio inverso |
| L6 | Listado completo de cambios, sin filtrar por canal | `CambiosGuardiasDBImpl.getCambiosGuardia` | Solo los registrados en la aplicación nueva (`Canal = 'APP'`). Los anteriores siguen en el listado del legacy, que no se retira | sigue en legacy |

### Huecos y decisiones

| ID | Hueco | Opciones | Decisión (2026-10-25) |
|---|---|---|---|
| H1 | Para registrar un cambio solo hay vista mensual: sin año, semana, día ni «hoy» (C1, C2) | (a) Aceptarlo en el piloto y registrarlo en la spec: el legacy sigue ofreciendo esas vistas para consultar. (b) Añadir vistas semana/día y «hoy» como tarea nueva | **Migrar** (b): FR-030 |
| H2 | Mover arrastrando y la bandeja entre meses se sustituyen por «elegir guardia → acción → formulario» (C4, C5) | (a) Aceptar la sustitución: mismo resultado y validación previa. (b) Añadir arrastrar y soltar en la vista mensual como alternativa | **Migrar** (b), a petición del responsable tras probar la interfaz: arrastrar y soltar con popup de Inicio y Fin (FR-032) |
| H3 | Sin colores ni leyenda por tipo de guardia; dentro del día, orden distinto al del legacy (C7, C8) | (a) Añadir colores y leyenda equivalentes, y el mismo orden que `_OrdenarGuardias` (cambio solo de interfaz, pequeño). (b) Aceptar el texto | **Migrar** (a): FR-031 |
| H4 | Listado sin búsqueda ni ordenación por columna; adaptación al móvil sin comprobar (L2, L3) | (a) Aceptar la lista mensual. (b) Añadir búsqueda por médico y ordenación | **Aceptar** (a): registrada en la spec; móvil, en T068 |
| H5 | Un médico no administrador no ve sus cambios en la aplicación nueva (L4) | (a) Fuera de alcance: los médicos no usan `/app/` en esta funcionalidad; ven sus guardias en el calendario legacy y los cambios antiguos en su listado. (b) Nueva funcionalidad | **Fuera de alcance** (a): registrada en la spec |

Decidido por David (responsable del proyecto) el 2026-10-25: H1 se migra («hay que migrarlo»), y el resto según la propuesta. Ese mismo día, tras probar la interfaz, cambió H2: se migra el arrastrar y soltar «como en el legacy, con un popup de inicio y fin, por usabilidad». H1 y H3 se implementan en T069–T071 y H2 en T072–T073 (tasks.md).

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
4. **Excepción al principio VI por el hash SHA-512 del legacy**, con el alcance y la revisión indicados en Complexity Tracking: aprobada (tras `/speckit-analyze`).

Pendiente operativo: fijar `ACTIVAR_CAMBIO_GUARDIAS = 'N'` en el servicio al activar la aplicación nueva (R13), dentro de la ventana de puesta en marcha.
