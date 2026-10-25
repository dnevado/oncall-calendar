# Implementation Plan: Gestión de vacaciones de los médicos

**Branch**: `002-gestion-vacaciones` (trabajo actual en `rearchitecture-claude-based-speckit`) | **Date**: 2026-10-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-gestion-vacaciones/spec.md`

## Summary

El administrador del servicio consulta, añade y quita días de vacaciones de los médicos de su servicio desde la aplicación nueva (`/app/`). La pantalla tiene dos partes:
- un resumen del servicio con los días en conflicto señalados;
- un calendario de selección múltiple por médico, que sustituye al MultiDatesPicker de la ficha legacy.

El backend Spring Boot comprueba el rol y las reglas VAC-003 y VAC-008. Aplica solo los días añadidos y quitados, en una transacción que bloquea el servicio igual que el registro de cambios de la 001. Escribe en la tabla legacy `vacaciones_medicos`, sin cambios de esquema, para que el legacy (agenda, CG-012, ficha y listado) vea lo mismo. El legacy no se modifica: el riesgo R1 está aceptado (FR-016). Detalle en [research.md](./research.md).

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript 5 en modo estricto (frontend), como en la 001.

**Primary Dependencies**:
- Las de la 001: Spring Boot 3.4.5, MyBatis, Liquibase, React 18, Vite, React Router, `openapi-fetch`.
- **Nueva:** `react-day-picker` 9.14.0, versión exacta, MIT, para el selector de varias fechas (research V6). Arrastra `date-fns` 4 (MIT).

**Storage**: MySQL 5.7 del legacy. Tablas `vacaciones_medicos` (lectura y escritura), `guardias_medicos`, `medicos` y `guardias_servicios_medicos` (lectura), y `guardias_servicios` (bloqueo). Sin cambios de esquema.

**Testing**: JUnit 5, Spring Boot Test, Testcontainers MySQL 5.7, validación OpenAPI, regresión con la SQL del legacy; Vitest, Testing Library, MSW.

**Target Platform**: la de la 001 (contenedores detrás de nginx, mismo dominio).

**Project Type**: aplicación web (API + SPA) en convivencia con el legacy.

**Performance Goals**: volumen bajo (300–600 días al año, unos 40 médicos). Objetivos:
- registrar un periodo en menos de 1 minuto (SC-001);
- respuesta de las operaciones en menos de 1 s.

**Constraints**:
- legacy en solo lectura, sin excepciones en esta funcionalidad;
- formato `yyyy-MM-dd` y clave única existentes;
- transacción atómica (FR-006);
- sin datos personales en pruebas ni documentos (principio VI).

**Scale/Scope**: 1 servicio en uso. Una pantalla nueva («Vacaciones») con resumen y editor; 2 operaciones de API.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio / norma | Cumplimiento en este plan | Estado |
|---|---|---|
| I. Reglas validadas | Todas las reglas proceden de VAC-001 a VAC-012, validadas el 2026-10-25, y de CG-012. Los límites de 366 fechas y 731 días son protecciones técnicas, declaradas como tales (research V7), no reglas de negocio. | ✅ |
| II. Reglas en el servidor | Rol (`ServicioAccess`), pertenencia del médico, VAC-003 y VAC-008 se comprueban en el backend. El selector solo prepara la operación (FR-020). Identidad tomada de la sesión. | ✅ |
| III. Migración incremental | Incremento acotado a vacaciones. El legacy no se modifica (FR-016, opción C). Paridad documentada en spec (Assumptions, desviaciones). | ✅ |
| IV. Contrato primero | Borrador 1.2.0 en [contracts/](./contracts/guardias-cambios.openapi.yaml) (solo añade). Se publica en `contracts/` antes de implementar. Errores con Problem Details y `ReglasIncumplidas`. | ✅ |
| V. Evidencia y pruebas | Reglas trazadas a `docs/legacy/vacaciones-discovery.md`. [quickstart.md](./quickstart.md) cubre cada regla y la regresión legacy. | ✅ |
| VI. Datos y secretos | Pruebas con datos ficticios. Las cifras de la copia de producción solo aparecen como agregados. Sin secretos nuevos. | ✅ |
| VII. Integridad de datos | Transacción con `lockServicio` (research V3), SQL parametrizada (`#{}`), clave única existente, altas idempotentes (`ON DUPLICATE KEY UPDATE`). | ✅ |
| Restricciones técnicas | Mismas tecnologías. Dependencia nueva justificada (research V6). Esquema legacy intacto. | ✅ |
| Flujo (punto 2) | Sin preguntas abiertas en la spec. FR-016 decidido el 2026-10-25. | ✅ |
| Flujo (punto 6) | Sin operaciones destructivas: no se limpian ni migran datos. Los 5 conflictos se muestran, no se corrigen. | ✅ |
| VIII. Capacidades de la interfaz | Inventario y cobertura en la sección siguiente. Sin huecos sin decidir. | ✅ |

**Resultado de la puerta (antes de la fase 0)**: superada, sin excepciones.

**Re-check tras la fase 1**: el contrato solo añade operaciones y códigos. El modelo no cambia el esquema. La concurrencia con la 001 queda cubierta por el mismo bloqueo. Sin nuevas desviaciones.

## Capacidades de la interfaz legacy (principio VIII)

Estados como en la [001](../001-registro-cambios-guardia/plan.md#capacidades-de-la-interfaz-legacy-principio-viii): **cubierta**, **sustituida**, **descartada por spec**, **sigue en legacy**, **hueco**.

### Ficha del médico: vacaciones (`web/medico/detallemedico.jsp`)

| # | Capacidad legacy | Evidencia | En la aplicación nueva | Estado |
|---|---|---|---|---|
| V1 | Marcar y desmarcar varios días sueltos sobre un calendario mensual | `detallemedico.jsp:372-383` (MultiDatesPicker 1.6.3) | `react-day-picker` en modo múltiple, con ratón y teclado; además, un periodo opcional que se guarda como días (FR-018) | cubierta (mejorada) |
| V2 | Ver marcadas las vacaciones ya guardadas | `:291-298`, `:590-595`, `addDates(holidays)` | Días guardados marcados; los que se van a añadir o quitar, distinguidos hasta guardar (FR-018, FR-019) | cubierta |
| V3 | Semana desde el lunes, días y meses en español | `:374-381` | `weekStartsOn={1}`, idioma `es` | cubierta |
| V4 | Ventana de selección de −1 mes a +12 meses | `:372` (solo cliente) | Sin días pasados ni límite futuro, comprobado en el servidor (FR-008, FR-009) | descartada por spec (VAC-004, VAC-008) |
| V5 | Guardar las vacaciones junto con los datos del médico | `:413-470`, `:205-248` | Guardado propio de las vacaciones, solo de los días tocados (FR-005); la ficha del médico sigue en el legacy | sustituida |
| V6 | Mensaje de error con el día en conflicto, o de éxito | `:143-165`, `:450-466` | Todos los días incumplidos con su motivo (FR-011) y resumen de lo aplicado | cubierta (mejorada) |
| V7 | Solo el administrador puede abrir el selector | `:29-35`, `:599` (solo cliente) | Permiso comprobado en el servidor (FR-001) | cubierta (mejorada) |
| V8 | El médico abre su propia ficha («Tu perfil») | `web/common/navigation.jsp:85,91` | Los médicos no ven vacaciones en la aplicación nueva (FR-002). La ficha sigue en el legacy | descartada por spec (VAC-009) |
| V9 | Ver las vacaciones de días pasados (desde 1 mes atrás) | `:291-298` | Consulta de cualquier periodo pasado, sin edición (FR-012) | cubierta (mejorada) |

### Listado de médicos (`web/medico/lista_medicos.jsp`)

| # | Capacidad legacy | Evidencia | En la aplicación nueva | Estado |
|---|---|---|---|---|
| M1 | Columna «Vacaciones» con los meses con vacaciones de cada médico en los próximos 6 meses | `lista_medicos.jsp:78`, `:98-110`, `:146-157` | Resumen del servicio: cada médico con sus días en el periodo elegido (por defecto, hoy + 6 meses), navegable a otros periodos (FR-013) | cubierta (mejorada: días, no solo meses) |
| M2 | Abrir la ficha del médico desde la fila | `:135` | Elegir el médico en el resumen abre su editor de vacaciones. El resto de la ficha sigue en el legacy | sustituida (solo vacaciones) |
| M3 | Médicos inactivos en el servicio, en rojo | `:121-122` (`ActivoServicio`) | Inactivos señalados con texto, no solo con color (FR-019). El indicador es el de actividad en el servicio, como el legacy | cubierta |
| — | Conflictos vacaciones y guardia | no existe | Señalados en el resumen y en el editor (FR-014) | nueva |

### Configuración y agenda

| # | Capacidad legacy | Evidencia | Estado |
|---|---|---|---|
| A1 | Meses vacacionales del servicio | `config_form.jsp:443-460` | sigue en legacy (VAC-011, P9 c) |
| A2 | La agenda no asigna guardias a quien está de vacaciones | `calc_agenda.jsp:525`, `fill_agenda.jsp:128` | sigue en legacy; se garantiza con la regresión del quickstart, escenario 12 (VAC-010) |

**Móvil**: el editor usa un mes por pantalla en anchos estrechos y dos meses en escritorio. Su verificación en móvil sigue la decisión H4 (móvil) de la 001: aplazada hasta antes de retirar el calendario legacy o el 2027-04-30.

## Project Structure

### Documentation (this feature)

```text
specs/002-gestion-vacaciones/
├── plan.md              # Este fichero
├── research.md          # Fase 0: decisiones V1–V9
├── data-model.md        # Fase 1: entidades, operación y correspondencia con reglas
├── quickstart.md        # Fase 1: escenarios de validación
├── contracts/
│   └── guardias-cambios.openapi.yaml   # Borrador 1.2.0 (añade vacaciones)
├── checklists/
│   └── requirements.md
└── tasks.md             # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
contracts/
└── guardias-cambios.openapi.yaml        # Se publica la 1.2.0

backend/src/main/java/es/medoncalls/guardias/
├── vacaciones/
│   ├── api/                              # VacacionesController y DTO según el contrato
│   ├── domain/                           # ReglasVacaciones (VAC-003, VAC-008), diferencias
│   ├── application/                      # ModificarVacacionesService (transacción, bloqueo), consulta
│   └── infrastructure/                   # VacacionMapper (+ XML): consulta con conflictos, altas, bajas
├── cambios/domain/ReglaCodigo.java       # + VAC-003, VAC-008 (o enum propio, ver tasks)
└── common/                               # sin cambios
backend/src/main/resources/mappers/VacacionMapper.xml
backend/src/test/java/es/medoncalls/guardias/
├── vacaciones/domain/                    # unitarias
├── integration/ModificarVacacionesIT.java, ConsultarVacacionesIT.java
├── contract/VacacionesContractTest.java
└── legacy/LegacyVacacionesIT.java        # SQL original de VacacionesDBImpl y NoTieneVacaciones

frontend/src/
├── App.tsx                               # ruta /servicios/:id/vacaciones y enlace en la navegación
└── features/vacaciones/
    ├── VacacionesPage.tsx                # resumen del servicio + editor
    ├── ResumenVacaciones.tsx
    ├── EditorVacaciones.tsx              # react-day-picker, diferencias, guardar
    ├── useVacaciones.ts
    ├── vacaciones.css                    # tokens existentes
    └── *.test.tsx
```

**Structure Decision**: módulo `vacaciones` paralelo a `cambios` en backend y frontend, con la misma separación dominio, aplicación, infraestructura y API. Se reutilizan seguridad, errores, reloj y bloqueo de la 001. No se toca el legacy ni el esquema.

## Complexity Tracking

Sin violaciones de la constitución que justificar.

| Decisión con coste | Motivo | Alcance y revisión |
|---|---|---|
| Dependencia nueva `react-day-picker` 9.14.0 | Selección múltiple accesible con teclado, que FullCalendar no ofrece (research V6) | Solo `features/vacaciones`. Revisión al migrar los selectores de fechas de formularios e informes (CLAUDE.md), para reutilizarla |
| Riesgo aceptado R1 del legacy (borrado de vacaciones al guardar la ficha) | Decisión del responsable (FR-016, opción C): no tocar el legacy | Mientras convivan. Revisión al migrar la ficha del médico |

## Approvals

Pendiente: revisión del plan por David (responsable del proyecto), en particular la dependencia nueva y la sección de capacidades.
