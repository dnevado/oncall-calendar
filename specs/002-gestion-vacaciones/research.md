# Research: Gestión de vacaciones de los médicos

Decisiones de la fase 0 para [plan.md](./plan.md). La base técnica (backend, frontend, despliegue, sesión, Problem Details, reloj) es la de la funcionalidad 001 ([research 001](../001-registro-cambios-guardia/research.md)) y no se repite aquí.

## V1 — Ubicación del código

- **Decisión:** módulo nuevo `vacaciones` en el backend (`api`, `application`, `domain`, `infrastructure`, como `cambios`) y en el frontend (`features/vacaciones`). Se reutilizan `auth`, `common.ServicioAccess`, `ProblemDetailsHandler`, el `Clock` del servicio y el bloqueo `GuardiaMapper.lockServicio`.
- **Razón:** separa la funcionalidad sin duplicar la seguridad ni el manejo de errores. El `VacacionesMapper` de `cambios` (solo lectura para CG-012) no se toca: la 001 no cambia.
- **Alternativas:** ampliar el mapper de `cambios` (mezcla dos funcionalidades y obliga a retocar código ya revisado).

## V2 — Operación por diferencias

- **Decisión:** `PATCH /servicios/{servicioId}/medicos/{medicoId}/vacaciones` con dos listas, `anadir` y `quitar`. Solo se insertan o borran esos días.
- **Razón:** es la regla VAC-007 (no tocar lo demás) y evita que dos administradores, o el legacy y la aplicación nueva, se pisen. Es idempotente por naturaleza: repetir la petición no cambia nada (FR-007; principio VII), sin necesidad de `Idempotency-Key`.
- **Alternativas:** `PUT` con el conjunto completo de días (reproduce el defecto R1 del legacy: lo que no llega se borra); una operación por día (pierde la atomicidad de FR-006).

## V3 — Concurrencia

- **Decisión:** la transacción empieza con `lockServicio(servicioId)` (`SELECT … FOR UPDATE` sobre `guardias_servicios`), el mismo bloqueo que el registro de cambios de la 001.
- **Razón:** sin él, un administrador podría añadir vacaciones un día mientras otro cede a ese médico una guardia del mismo día: cada operación vería «sin conflicto» (VAC-003 y CG-012 incumplidas a la vez). Con el mismo bloqueo, las dos operaciones se serializan dentro del servicio. VAC-003 solo mira el servicio del administrador (P1 a), así que basta con el bloqueo del servicio.
- **Escrituras del legacy:** no toman este bloqueo. Las altas usan `INSERT … ON DUPLICATE KEY UPDATE` sobre la clave única `IND_VACACIONES_01`, de modo que un día insertado a la vez por el legacy no hace fallar la operación; las bajas borran por clave. El riesgo R1 del legacy está aceptado (spec, FR-016).
- **Alternativas:** bloquear la fila del médico en `medicos` (obliga a cambiar la 001 para que también la bloquee); nivel de aislamiento `SERIALIZABLE` (más bloqueos de los necesarios).

## V4 — Esquema

- **Decisión:** sin cambios de esquema ni changesets de Liquibase. Se usa `vacaciones_medicos (IdMedico, FDay varchar(10))` con fechas `yyyy-MM-dd` (`FechaLegacy`).
- **Razón:** la tabla ya garantiza un registro por médico y fecha (VAC-005). Los datos actuales cumplen el formato (0 filas con otro formato y 0 huérfanas, discovery §4). El legacy lee la tabla con SQL concatenado y comparaciones de texto, y no debe notar ningún cambio (FR-016).
- **Alternativas descartadas:** columna de tipo fecha o clave ajena a `medicos` (rompen o arriesgan el legacy durante la convivencia); columnas de auditoría (la spec no las pide: «Sin historial»).

## V5 — Conflictos

- **Decisión:** se calculan al leer, con un `LEFT JOIN` a `guardias_medicos` por médico, fecha (`LEFT(FGuardia, 10)`) y servicio. No se guardan.
- **Razón:** un conflicto aparece o desaparece cuando cambian las guardias (agenda, cambios, legacy). Guardarlo obligaría a mantenerlo desde todos esos sitios.

## V6 — Selector de fechas

- **Decisión:** `react-day-picker` 9.14.0 (MIT, publicada el 2026-02-26), versión exacta. Usa `mode="multiple"`, `weekStartsOn={1}`, el idioma `es` de `react-day-picker/locale`, `disabled={{ before: hoy }}` y modificadores para vacaciones guardadas, días a añadir o quitar y conflictos. Opcionalmente, `mode="range"` para elegir un periodo (FR-018, MAY), que se convierte en días sueltos.
- **Razón:** conserva la capacidad del MultiDatesPicker (principio VIII) con un componente maduro. Es accesible: rejilla ARIA y navegación con teclado (FR-019). Trae su propio CSS sin estilos en línea problemáticos para la CSP (`style-src 'self' 'unsafe-inline'` ya lo permite). Su dependencia `date-fns` 4 es MIT.
- **Alternativas:**
  - FullCalendar 6, ya instalado. No tiene selección múltiple de días sueltos y su `dateClick` no se puede usar con teclado. Habría que construir la selección a mano.
  - Un calendario hecho a mano. Más código y más riesgo de accesibilidad (CLAUDE.md: componentes maduros antes que construir).
  - `react-multi-date-picker`. Menos usado y basado en estilos propios más difíciles de adaptar al sistema de tokens.

## V7 — Errores y respuesta

- **Decisión:** 422 `ReglasIncumplidas` (el mismo esquema que la 001) con una infracción por día y regla: `VAC-008` (día anterior a hoy, en `anadir` o `quitar`) y `VAC-003` (día con guardia en el servicio, solo en `anadir`, con la guardia en el mensaje). 400 para peticiones mal formadas: listas vacías, fecha repetida en las dos listas, más de 366 fechas, rango de consulta inválido. 404 si el médico no pertenece al servicio. 403 si el usuario no administra el servicio. La respuesta 200 indica los días realmente añadidos y quitados.
- **Razón:** FR-011 (todos los días incumplidos con su motivo) y constitución, principio IV (errores estructurados).
- **Límites técnicos** (366 fechas por lista, 731 días por consulta): protegen al servidor. No son reglas de negocio. Un año de días cabe en una petición.

## V8 — «Hoy»

- **Decisión:** `LocalDate.now(clock)` con la zona `guardias.zona-horaria` (`ClockConfig`). El día de hoy se puede añadir y quitar (spec, Assumptions).
- **Razón:** misma referencia que las reglas CG-008 y CG-009 de la 001. En las pruebas se fija con `RelojPrueba`.

## V9 — Pruebas

- **Decisión:**
  - **Unitarias** (dominio): validaciones VAC-003 y VAC-008, y cálculo de diferencias.
  - **Integración** (Testcontainers, MySQL 5.7): permisos (403/404), atomicidad, idempotencia y concurrencia con una cesión del mismo día.
  - **Contrato:** validación OpenAPI.
  - **Regresión legacy** (SQL original de `VacacionesDBImpl` y `NoTieneVacaciones` sobre filas escritas por la API, SC-006).
  - **Frontend:** Vitest y Testing Library con MSW, incluidas las pruebas de teclado del selector.
- **Razón:** constitución, principio V. La regresión legacy es la que demuestra FR-016 sin tocar el legacy (P9 c).
