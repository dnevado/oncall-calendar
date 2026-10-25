# Feature Specification: Gestión de vacaciones de los médicos

**Feature Branch**: `rearchitecture-claude-based-speckit` (directorio de la funcionalidad: `specs/002-gestion-vacaciones`)

**Created**: 2026-10-25

**Status**: Aprobada el 2026-10-25 (FR-016 decidido el 2026-10-25)

**Input**: User description: "Gestión de vacaciones de los médicos por el administrador del servicio. El administrador consulta, añade y quita días de vacaciones de los médicos de su servicio, eligiendo varias fechas a la vez; los cambios se aplican en el acto, sin aprobación ni avisos. Fuentes de verdad: reglas validadas en docs/rules/vacaciones-reglas-candidatas.md (BR-CAND-VAC-001 a 012, todas validadas el 2026-10-25) y evidencias legacy en docs/legacy/vacaciones-discovery.md. […] Fuera de alcance: generación de agenda, configuración de meses vacacionales, notificaciones, gestión por el propio médico, multiservicio."

**Fuentes de verdad**:
- Reglas validadas: [docs/rules/vacaciones-reglas-candidatas.md](../../docs/rules/vacaciones-reglas-candidatas.md) (respuestas del 2026-10-25). En este documento se citan como `VAC-NNN` (abreviatura de `BR-CAND-VAC-NNN`). Se cita también `CG-012` del [catálogo de cambios](../../docs/rules/cambio-guardia-reglas-candidatas.md).
- Evidencia legacy: [docs/legacy/vacaciones-discovery.md](../../docs/legacy/vacaciones-discovery.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Añadir días de vacaciones a un médico (Priority: P1)

El Dr. B comunica al administrador que estará de vacaciones del 3 al 14 de agosto y el 24 de diciembre. El administrador abre las vacaciones del Dr. B, ve en un calendario los días que ya tiene, marca de una vez los días nuevos y guarda. Los días quedan registrados en el acto y, desde ese momento, el Dr. B no puede recibir guardias esos días ni la agenda se las asigna.

**Why this priority**: es el uso principal: cada año se registran entre 300 y 600 días de vacaciones, sobre todo en verano y Navidad, y la generación de agenda depende de ellos. Es el mínimo que permite dejar de usar el selector de vacaciones de la ficha del legacy.

**Independent Test**: con datos de prueba, añadir varios días a un médico y comprobar que quedan registrados, que la regla de cambios CG-012 los tiene en cuenta y que la agenda legacy no asigna guardias esos días. Intentar añadir días que incumplen cada regla y comprobar el rechazo.

**Acceptance Scenarios**:

1. **Given** el Dr. B pertenece al servicio y no tiene guardias del 3 al 14 de agosto ni el 24 de diciembre (fechas futuras), **When** el administrador marca esos 13 días y guarda, **Then** los 13 días quedan registrados como vacaciones del Dr. B y las vacaciones que ya tenía no cambian (VAC-002, VAC-007).
2. **Given** el Dr. B tiene una guardia en el servicio el 10 de agosto, **When** el administrador intenta añadir del 3 al 14 de agosto, **Then** el sistema rechaza la operación indicando el 10 de agosto y la guardia, y no registra ninguno de los días (VAC-003, VAC-007).
3. **Given** hoy es 25 de octubre, **When** el administrador intenta añadir el 20 de octubre, **Then** se rechaza por ser un día pasado (VAC-008). **When** añade el 25 de octubre (hoy), **Then** se acepta.
4. **Given** una fecha dentro de tres años, **When** el administrador la añade, **Then** se acepta: no hay límite hacia el futuro (VAC-004).
5. **Given** el Dr. B ya tiene vacaciones el 5 de agosto, **When** el administrador vuelve a marcar el 5 de agosto junto con otros días, **Then** se acepta sin duplicar el día (VAC-005).
6. **Given** el Dr. B tiene vacaciones el 10 de agosto, **When** se intenta cederle una guardia de ese día, **Then** la cesión se rechaza por vacaciones (CG-012, ya implementada en la funcionalidad 001).

---

### User Story 2 - Quitar días de vacaciones (Priority: P1)

El Dr. B cancela parte de sus vacaciones. El administrador abre sus vacaciones, desmarca los días que ya no se toma y guarda. Solo esos días dejan de ser vacaciones; el resto se mantiene.

**Why this priority**: sin poder quitar días, cualquier error o cambio de planes obliga a volver al legacy, cuyo guardado borra vacaciones que no se han tocado (discovery R1).

**Independent Test**: con datos de prueba, quitar algunos días y comprobar que solo esos desaparecen; intentar quitar un día pasado y comprobar el rechazo.

**Acceptance Scenarios**:

1. **Given** el Dr. B tiene vacaciones del 3 al 14 de agosto y el 24 de diciembre, **When** el administrador quita el 13 y el 14 de agosto y guarda, **Then** solo esos dos días dejan de ser vacaciones (VAC-007).
2. **Given** el Dr. B tuvo vacaciones el 1 de octubre (día pasado), **When** el administrador intenta quitarlo, **Then** se rechaza y el día se mantiene (VAC-008).
3. **Given** el administrador quita unos días y añade otros en la misma operación y uno de los añadidos tiene guardia, **When** guarda, **Then** se rechaza la operación entera y no se quita ni se añade nada (VAC-003, VAC-007).

---

### User Story 3 - Ver las vacaciones del servicio y los conflictos (Priority: P2)

El administrador quiere saber, de un vistazo, quién tiene vacaciones en los próximos meses y si hay días de vacaciones que coinciden con una guardia. Abre la vista de vacaciones del servicio y ve, para cada médico, sus próximas vacaciones, con los días en conflicto señalados.

**Why this priority**: el legacy ofrece este resumen en el listado de médicos (meses con vacaciones de los próximos 6 meses). Los conflictos son nuevos: hoy hay 5 días de vacaciones que coinciden con una guardia y nadie los ve (VAC-009).

**Independent Test**: con datos de prueba que incluyan un día de vacaciones con guardia del mismo médico, abrir la vista del servicio y comprobar que aparecen las vacaciones de cada médico y que ese día está señalado como conflicto.

**Acceptance Scenarios**:

1. **Given** varios médicos del servicio con vacaciones en los próximos 6 meses, **When** el administrador abre la vista del servicio, **Then** ve para cada médico en qué meses y qué días tiene vacaciones (VAC-009).
2. **Given** el Dr. C tiene vacaciones y una guardia en el servicio el mismo día, **When** el administrador ve sus vacaciones, **Then** ese día aparece señalado como conflicto, indicando la guardia, y el sistema no lo corrige por su cuenta (VAC-009).
3. **Given** un usuario sin rol de administrador del servicio, **When** intenta ver las vacaciones, incluso sin pasar por la pantalla, **Then** se le deniega el acceso (VAC-001, VAC-009).

---

### Edge Cases

- **Restricción de permisos**: un usuario que no es administrador del servicio intenta añadir, quitar o consultar vacaciones, incluso llamando directamente al sistema sin pasar por la pantalla. Se rechaza y no se modifica nada (VAC-001; constitución, principio II).
- **Médico de otro servicio**: el administrador intenta gestionar las vacaciones de un médico que no pertenece a su servicio. Se rechaza.
- **Varios conflictos**: si varios días añadidos tienen guardia, el sistema informa de todos, con su fecha (el legacy se detenía en el primero).
- **Conflicto en otro servicio**: el médico tiene guardia ese día en otro servicio. No se considera conflicto al añadir vacaciones (VAC-003, solo el servicio del administrador). Las vacaciones, una vez registradas, sí impiden recibir guardias en cualquier servicio (VAC-006, CG-012).
- **Quitar un día en conflicto**: un día de vacaciones futuro que coincide con una guardia se puede quitar como cualquier otro.
- **Días repetidos o ya inexistentes**: añadir un día que ya es de vacaciones o quitar uno que ya no lo es no es un error y no cambia nada; repetir la misma operación da el mismo resultado (principio VII).
- **Dos administradores a la vez**: cada uno aplica solo sus días añadidos y quitados; ninguno borra lo que el otro ha añadido.
- **Fallo a mitad de operación**: no se guarda nada (VAC-007; principio VII).
- **Médico inactivo**: sus vacaciones se pueden consultar y gestionar igual que las de un médico activo (ver Assumptions).
- **Convivencia con el legacy**: las vacaciones que se añaden en el legacy se ven en la aplicación nueva y viceversa. Guardar la ficha de un médico en el legacy puede seguir borrando vacaciones futuras (discovery R1); riesgo aceptado en FR-016.

## Requirements *(mandatory)*

### Functional Requirements

**Permisos**

- **FR-001**: El sistema MUST permitir consultar, añadir y quitar vacaciones solo a usuarios con rol de administrador del servicio al que pertenece el médico, y MUST comprobarlo en el servidor en cada operación (VAC-001, VAC-009).
- **FR-002**: Los médicos sin rol de administrador MUST NOT poder consultar ni modificar vacaciones en la aplicación nueva, ni las suyas (VAC-009).

**Operación**

- **FR-003**: El administrador MUST poder añadir y quitar varios días de vacaciones de un médico en una sola operación (VAC-005).
- **FR-004**: La operación MUST aplicarse en el acto, sin aprobación ni estados intermedios (VAC-002).
- **FR-005**: La operación MUST afectar solo a los días añadidos y quitados; el resto de vacaciones del médico MUST quedar intacto (VAC-007).
- **FR-006**: La operación MUST ser atómica: si cualquier día incumple una regla o se produce un error, no se añade ni se quita ningún día (VAC-007; principio VII).
- **FR-007**: Añadir un día que ya es de vacaciones o quitar uno que no lo es MUST NOT producir error ni duplicados; un médico tiene como máximo un registro por fecha (VAC-005; principio VII).

**Reglas de validación** (evaluadas en el servidor)

- **FR-008**: El sistema MUST rechazar añadir o quitar días anteriores a hoy; el día de hoy está permitido (VAC-008).
- **FR-009**: No hay límite de fechas hacia el futuro (VAC-004).
- **FR-010**: El sistema MUST rechazar añadir un día de vacaciones en el que el médico tiene una guardia en el servicio del administrador. Las guardias en otros servicios no se consideran (VAC-003).
- **FR-011**: Cuando la operación se rechace, el sistema MUST informar de todos los días que incumplen alguna regla, indicando para cada uno la fecha y el motivo.

**Consulta**

- **FR-012**: El administrador MUST poder ver los días de vacaciones de cada médico del servicio: los futuros para editarlos y los pasados como consulta (VAC-009).
- **FR-013**: El administrador MUST poder ver un resumen de las vacaciones de los médicos del servicio en los próximos 6 meses como mínimo (paridad con el listado de médicos del legacy).
- **FR-014**: Los días de vacaciones que coinciden con una guardia del médico en el servicio MUST mostrarse señalados como conflicto, indicando la guardia; el sistema MUST NOT corregirlos automáticamente (VAC-009, P7).

**Convivencia y datos**

- **FR-015**: Las vacaciones son del médico, no del servicio: un día registrado MUST impedir que el médico reciba guardias en cualquier servicio, también por cambios registrados en la funcionalidad 001 (VAC-006, CG-012).
- **FR-016**: Mientras convivan legacy y sistema nuevo, las vacaciones MUST ser las mismas en ambos: lo que se registra en uno se ve en el otro, y la generación y el relleno de la agenda del legacy MUST seguir sin asignar guardias a quien está de vacaciones (VAC-010, VAC-011).
  - **Decisión (2026-10-25, opción C):** el legacy no se modifica. Se acepta durante la convivencia el riesgo de que guardar la ficha de un médico en el legacy borre sus vacaciones futuras (discovery R1, también desde «Tu perfil»). La aplicación nueva no lo evita ni lo detecta.
- **FR-017**: Esta funcionalidad MUST NOT enviar notificaciones (VAC-012).

**Interfaz: capacidades del legacy (constitución, principio VIII)**

- **FR-018**: El administrador MUST poder elegir y quitar varias fechas a la vez sobre un calendario mensual, con las vacaciones ya registradas marcadas, la semana empezando en lunes y textos y fechas en español (capacidad del selector legacy de `web/medico/detallemedico.jsp:372-383`). La interfaz MAY ofrecer además elegir un periodo de días seguidos, que se guarda como días sueltos (VAC-005).
- **FR-019**: El calendario MUST distinguir los días de vacaciones, los días en conflicto y los días no editables (pasados) sin depender solo del color, y MUST poder usarse con teclado.
- **FR-020**: Elegir fechas en la pantalla solo prepara la operación: las reglas FR-008 a FR-010 MUST validarse en el servidor al guardar, aunque la pantalla avise antes.

### Key Entities *(include if feature involves data)*

- **Día de vacaciones**: un día en que un médico está de vacaciones. Pertenece al médico, no al servicio; como máximo uno por médico y fecha. No tiene estado ni historial de quién lo registró.
- **Médico**: persona que hace guardias; puede pertenecer a varios servicios (hoy se usa uno).
- **Guardia**: asignación de un médico a un día en un servicio; se usa para detectar conflictos.
- **Servicio**: unidad organizativa con sus médicos y sus administradores; delimita qué médicos puede gestionar cada administrador.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un administrador registra un periodo de vacaciones de un médico (por ejemplo, 2 semanas) en menos de 1 minuto desde que abre la pantalla.
- **SC-002**: En el 100 % de las operaciones de prueba, las vacaciones que no se han tocado siguen igual después de guardar (0 pérdidas).
- **SC-003**: El 100 % de los casos de prueba que incumplen alguna regla (FR-008, FR-010) se rechazan, también cuando se intentan sin pasar por la pantalla, y no se modifica ningún día.
- **SC-004**: En el 100 % de los rechazos, el mensaje identifica todas las fechas incumplidas y el motivo, de forma comprensible para el administrador sin ayuda técnica.
- **SC-005**: Un usuario sin rol de administrador no consigue ver ni modificar vacaciones (0 casos en las pruebas de permisos).
- **SC-006**: El 100 % de los días registrados en la aplicación nueva son tenidos en cuenta por la generación de agenda del legacy y por la regla de cambios CG-012 en las pruebas.
- **SC-007**: Los 5 conflictos existentes en los datos se muestran señalados al administrador.

## Assumptions

- **Usuarios**: los administradores del servicio (hoy, dos en el servicio principal) registran las vacaciones que les comunican los médicos (VAC-001).
- **Alcance excluido**: generación de agenda y configuración de meses vacacionales (VAC-010, VAC-011: no se migran, solo se garantiza que siguen funcionando), notificaciones (VAC-012), gestión o consulta por el propio médico (VAC-009), multiservicio (R-d), aprobación (VAC-002), límites de días por médico o de plantilla mínima (no existen en el legacy) y mostrar las vacaciones en el calendario de guardias.
- **Médicos gestionables**: todos los médicos dados de alta en el servicio del administrador, activos o inactivos, como en el listado del legacy.
- **«Hoy»**: se toma la fecha del día en la zona horaria del servicio; el día de hoy no es pasado (VAC-008).
- **Datos existentes**: las 4 105 filas actuales (2017–2026) se conservan sin cambios; no se migran ni se limpian. Los 5 conflictos actuales se muestran, no se corrigen.
- **Esquema**: se usa la tabla de vacaciones existente con su formato de fecha; cualquier cambio de esquema debe ser compatible con el legacy (constitución, restricciones técnicas).
- **Sin historial**: no se registra quién ni cuándo añadió o quitó cada día, como en el legacy.
- **Conflicto por servicio (P1)**: al añadir vacaciones solo se miran las guardias del servicio del administrador, aunque las vacaciones bloqueen en todos los servicios. Con un único servicio en uso no hay diferencia práctica.
- **Desviaciones intencionadas respecto al legacy**: FR-001/FR-002 (permiso en el servidor, el médico no ve sus vacaciones), FR-005/FR-006 (solo días tocados, atómico), FR-008 (sin días pasados; el legacy permitía un mes atrás), FR-009 (sin límite futuro; el legacy, 12 meses), FR-011 (todos los conflictos), FR-014 (conflictos señalados) y la separación de las vacaciones de la ficha del médico.
- **Rendimiento**: volumen bajo (unos 300–600 días al año en el servicio principal, 16 médicos con vacaciones en el histórico); se asumen tiempos de respuesta estándar de una aplicación web.
