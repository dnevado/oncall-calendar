# Feature Specification: Registro de cambios y cesiones de guardia

**Feature Branch**: `rearchitecture-claude-based-speckit` (directorio de la funcionalidad: `specs/001-registro-cambios-guardia`)

**Created**: 2026-10-24

**Status**: Approved — David (Administrador, responsable del proyecto), 2026-10-24. Incluye las reglas que no proceden del catálogo: FR-015, FR-016 y FR-029.

**Input**: User description: "Registro de cambios y cesiones de guardia por el administrador del servicio. Un administrador registra, en nombre de los médicos, un CAMBIO (intercambio de dos guardias entre dos médicos en dos fechas) o una CESION (un médico entrega su guardia de una fecha a otro médico); el cambio se aplica al calendario en el acto, sin estados intermedios. Fuentes de verdad: reglas validadas en docs/rules/cambio-guardia-reglas-candidatas.md (BR-CAND-CG-002, 003, 005–015, 017–021 validadas; 001, 004, 023 descartadas; 016 y 022 pendientes) y evidencias legacy en docs/legacy/cambio-guardia-discovery.md. Incluye el efecto en el equilibrio del reparto (CG-020) y los requisitos derivados: histórico de tipo de médico, tipo/festivo de la guardia cedida guardado en la cesión y unicidad médico+fecha+servicio. Fuera de alcance: solicitud por el médico, aprobación, Google Calendar. Preguntas abiertas: duplicados (CG-016), notificaciones (CG-022) y cómo corregir un cambio registrado por error."

**Fuentes de verdad**:
- Reglas validadas: [docs/rules/cambio-guardia-reglas-candidatas.md](../../docs/rules/cambio-guardia-reglas-candidatas.md) (firmadas el 2026-10-24). En este documento se citan como `CG-NNN` (abreviatura de `BR-CAND-CG-NNN`).
- Evidencia legacy: [docs/legacy/cambio-guardia-discovery.md](../../docs/legacy/cambio-guardia-discovery.md).
- Respuestas de negocio: [docs/legacy/cambio-guardia-cuestionario-negocio.md](../../docs/legacy/cambio-guardia-cuestionario-negocio.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar una cesión de guardia (Priority: P1)

El administrador del servicio recibe el aviso de que la Dra. A no puede hacer su guardia del día 10 y de que el Dr. B la hará en su lugar. El administrador elige la guardia de la Dra. A del día 10, indica que es una cesión y selecciona al Dr. B. El sistema comprueba las reglas y, si se cumplen, el Dr. B pasa a tener esa guardia (mismo tipo y festivo) y la Dra. A deja de tenerla. Si alguna regla no se cumple, el sistema rechaza la cesión e indica qué regla falla y para quién.

**Why this priority**: es el caso más frecuente (3.582 de 5.484 cambios en el histórico) y es el mínimo que permite retirar la pantalla de cambios del legacy.

**Independent Test**: se puede probar completo con un calendario de prueba: registrar una cesión válida y comprobar el calendario resultante; registrar cesiones que incumplen cada regla y comprobar el rechazo y el mensaje.

**Acceptance Scenarios**:

1. **Given** la Dra. A tiene guardia de presencia el día 10 (fecha futura) y el Dr. B es adjunto como ella, está activo, no tiene guardia ni vacaciones el día 10 y no tiene presencia los días 9 ni 11, **When** el administrador registra la cesión del día 10 de A a B, **Then** el día 10 la guardia de presencia pasa a ser del Dr. B, con el mismo festivo, y la Dra. A ya no aparece ese día.
2. **Given** el Dr. B ya tiene una guardia el día 10, **When** el administrador intenta registrar la cesión, **Then** el sistema la rechaza indicando que el Dr. B ya tiene guardia ese día y el calendario no cambia (CG-011).
3. **Given** el Dr. B tiene vacaciones el día 10 (en cualquier servicio), **When** se intenta la cesión, **Then** se rechaza indicando las vacaciones del Dr. B (CG-012).
4. **Given** la guardia cedida es de presencia y el Dr. B tiene presencia el día 9 o el día 11, **When** se intenta la cesión, **Then** se rechaza indicando el día de presencia consecutiva, también si el Dr. B es residente simulado (CG-013).
5. **Given** el Dr. B es residente y la Dra. A es adjunta en la fecha de la cesión, **When** se intenta la cesión, **Then** se rechaza por tipo de médico distinto (CG-014).
6. **Given** el Dr. B está inactivo, **When** el administrador busca receptores, **Then** el Dr. B no aparece como candidato, y si se intenta igualmente la cesión se rechaza (CG-015).
7. **Given** la guardia de la Dra. A es de hoy o de una fecha pasada, **When** se intenta cederla, **Then** se rechaza (CG-008).
8. **Given** el Dr. B no tiene ninguna guardia en ningún otro día, **When** se registra una cesión válida a su favor, **Then** se acepta: no se exige que el receptor tenga guardia en otra fecha (CG-007).

---

### User Story 2 - Registrar un intercambio de guardias (Priority: P1)

La Dra. A y el Dr. B acuerdan intercambiar la guardia de A del día 10 por la de B del día 15. El administrador registra el cambio indicando ambas guardias. Si se cumplen las reglas, el día 10 pasa a ser del Dr. B y el día 15 de la Dra. A; el tipo de guardia y el festivo se quedan con cada día.

**Why this priority**: es el segundo caso de uso (1.902 cambios en el histórico) y, junto con la cesión, completa la funcionalidad que hoy cubre el legacy.

**Independent Test**: con un calendario de prueba, registrar un intercambio válido y comprobar ambos días; registrar intercambios que incumplen cada regla y comprobar el rechazo.

**Acceptance Scenarios**:

1. **Given** A tiene guardia localizada el día 10 y B tiene guardia de presencia festiva el día 15, ambos del mismo tipo de médico y activos, sin vacaciones ni guardias en el día que reciben, **When** el administrador registra el intercambio, **Then** el día 10 es del Dr. B como localizada y el día 15 de la Dra. A como presencia festiva (CG-017).
2. **Given** el Dr. B no tiene guardia el día 15, **When** se intenta el intercambio, **Then** se rechaza (CG-006).
3. **Given** el día 15 es anterior a hoy, **When** se intenta el intercambio, **Then** se rechaza: no se reciben guardias pasadas (CG-009).
4. **Given** la Dra. A tiene presencia el día 14 y la guardia del día 15 es de presencia, **When** se intenta el intercambio, **Then** se rechaza por presencias consecutivas de la Dra. A.
5. **Given** la Dra. A tiene presencia el día 11 y el día 10 es la otra fecha del propio intercambio, **When** se evalúan las presencias consecutivas del Dr. B en torno al día 10, **Then** el día adyacente que coincide con la otra fecha del cambio no se considera (CG-013, excepción mantenida).

---

### User Story 3 - Ajuste automático de las guardias de adjunto por residentes R3–R5 (Priority: P2)

Cuando un cambio o cesión entre residentes hace que un día pase a tener, o deje de tener, un residente R3, R4 o R5, el sistema ajusta las guardias de adjunto de ese día: de refuerzo a localizada si entra un R3–R5, y de localizada a refuerzo si sale.

**Why this priority**: mantiene la coherencia del calendario según el nivel de los residentes; sin ella, el calendario resultante sería incorrecto, pero solo afecta a cambios entre residentes.

**Independent Test**: con un calendario de prueba con residentes de distintos niveles, registrar cambios que añadan o quiten un R3–R5 de un día y comprobar el tipo de las guardias de adjunto de ese día.

**Acceptance Scenarios**:

1. **Given** el día 10 tiene un residente R2 y un adjunto de refuerzo, **When** un residente R4 recibe la guardia del R2 ese día, **Then** la guardia del adjunto pasa a localizada (CG-019).
2. **Given** el día 10 tiene un residente R3 y un adjunto localizado, **When** un residente R1 recibe la guardia del R3 ese día, **Then** la guardia del adjunto pasa a refuerzo.
3. **Given** cedente y receptor son ambos R3–R5 (o ambos no lo son), **When** se registra el cambio, **Then** las guardias de adjunto no cambian.
4. **Given** el residente R3–R5 es el receptor y no el solicitante, **When** se registra el cambio, **Then** el ajuste se aplica igualmente.

---

### User Story 4 - Datos de las cesiones para el equilibrio del reparto (Priority: P2)

Al generar la agenda, el reparto equilibra el número de guardias de cada tipo entre médicos, y una guardia cedida debe contar para quien la tenía asignada (el cedente), con su tipo y su festivo originales, salvo en los meses vacacionales (CG-020). El generador de agenda sigue en el legacy y calcula mal las cesiones (discovery §6, «Cómputo de cesiones en el reparto»). En esta funcionalidad, cada cesión guarda el tipo y el festivo de la guardia cedida para que el cálculo correcto pueda hacerse sin reconstruir el pasado. La corrección del cálculo llega cuando se migre el generador; el cálculo del legacy no se modifica.

**Why this priority**: sin estos datos, el equilibrio correcto no podrá calcularse después para las cesiones registradas desde ahora.

**Independent Test**: registrar cesiones de distintos tipos y festivos y comprobar que cada registro guarda el tipo y el festivo de la guardia cedida, aunque el receptor tenga otras guardias de otro tipo.

**Acceptance Scenarios**:

1. **Given** la Dra. A tiene una presencia festiva el día 10, **When** el administrador registra su cesión al Dr. B, **Then** el registro de la cesión guarda «presencia» y «festivo», cedente A y receptor B.
2. **Given** el Dr. B tiene una localizada no festiva otro día, **When** se registra la cesión anterior, **Then** el registro conserva el tipo y el festivo de la guardia cedida, no los de otras guardias del receptor.
3. **Given** la cesión se ha registrado, **When** el legacy genera la agenda, **Then** su cálculo de equilibrio no cambia respecto al actual (sin regresión), mientras convivan ambos sistemas.

---

### Edge Cases

- **Restricción de permisos**: un usuario que no es administrador del servicio intenta registrar un cambio, incluso llamando directamente al sistema sin pasar por la pantalla. Se rechaza y no se modifica nada (CG-002; constitución, principio II).
- **Médico de otro servicio**: el administrador intenta ceder a un médico que no pertenece al servicio. Se rechaza.
- **Cambios simultáneos**: dos administradores registran a la vez cambios que afectan a la misma guardia. Solo uno puede aplicarse; el otro se rechaza porque la guardia ya no está asignada al cedente (CG-005), sin dejar el calendario a medias.
- **Fallo durante la aplicación**: si se produce un error a mitad de la aplicación, no se guarda nada: ni el cambio ni ninguna modificación del calendario (CG-021; principio VII).
- **Varias reglas incumplidas**: el sistema informa de todas las reglas incumplidas, no solo de la última (el legacy solo mostraba una).
- **Cambio repetido**: registrar dos veces el mismo cambio. El segundo intento se rechaza porque el cedente ya no tiene la guardia (CG-005) o el receptor ya la tiene (CG-011).
- **Mismo médico**: cedente y receptor son la misma persona. Se rechaza.
- **Cambio de tipo de médico**: un residente que pasó a adjunto el día 1 del mes. Para un cambio con fecha anterior se usa su tipo de residente; para uno posterior, el de adjunto (CG-014).
- **Registro por error**: el administrador se equivoca de receptor. Se corrige registrando el cambio inverso como un cambio normal (FR-028), que pasa por todas las reglas. Si el inverso incumple alguna (por ejemplo, porque la fecha ya no es futura), no se puede corregir desde la aplicación.

## Requirements *(mandatory)*

### Functional Requirements

**Permisos y registro**

- **FR-001**: El sistema MUST permitir registrar cambios y cesiones solo a usuarios con rol de administrador del servicio al que pertenecen las guardias, y MUST comprobarlo en el servidor en cada operación (CG-002).
- **FR-002**: El sistema MUST registrar el cambio y aplicarlo al calendario en una sola operación, sin estados intermedios (pendiente, aprobado, rechazado), sin aceptación del receptor y sin cancelación posterior por el solicitante (CG-003).
- **FR-003**: El sistema MUST guardar en cada cambio: tipo (CAMBIO o CESION), fecha origen, médico cedente, médico receptor, fecha destino (solo en CAMBIO), tipo de guardia y festivo de cada guardia afectada tal como estaban antes del cambio, servicio, administrador que lo registró y fecha y hora del registro (CG-007, CG-020).
- **FR-004**: El sistema MUST tener siempre disponible el registro de cambios; no existe un interruptor por servicio (CG-001 descartada).

**Reglas de validación** (todas evaluadas en el servidor; el sistema MUST rechazar el cambio y no modificar nada si alguna falla)

- **FR-005**: La guardia origen MUST estar asignada al cedente en la fecha origen (CG-005).
- **FR-006**: En un CAMBIO, el receptor MUST tener guardia en la fecha destino (CG-006).
- **FR-007**: En una CESION, el sistema MUST NOT exigir que el receptor tenga guardia en otra fecha (CG-007).
- **FR-008**: La fecha origen MUST ser posterior a hoy (CG-008). No se exige antelación mínima (CG-010).
- **FR-009**: En un CAMBIO, la fecha destino MUST ser posterior a hoy (CG-009).
- **FR-010**: Ningún médico MUST quedar con dos guardias el mismo día en el mismo servicio tras el cambio (CG-011).
- **FR-011**: Ningún médico MUST recibir una guardia en un día en que tiene vacaciones; las vacaciones de un médico cuentan en todos sus servicios (CG-012).
- **FR-012**: Ningún médico MUST quedar, tras el cambio, con guardia de presencia el día anterior o el posterior a una guardia de presencia que recibe. Sin exención para residentes simulados. No se considera el día adyacente que coincide con la otra fecha del propio cambio. Solo aplica a guardias de presencia (CG-013).
- **FR-013**: Cedente y receptor MUST ser del mismo tipo de médico (adjunto o residente) según el tipo que tenía cada uno en la fecha de la guardia afectada; aplica también cuando registra el administrador (CG-014).
- **FR-014**: El receptor MUST estar activo en general (no dado de baja como médico) y pertenecer al servicio, es decir, estar dado de alta en él; no se exige estar activo específicamente en ese servicio (CG-015). En un CAMBIO, esta regla solo se aplica al receptor, no al cedente aunque también reciba una guardia (decisión del responsable del proyecto, 2026-10-24).
- **FR-015**: Cedente y receptor MUST ser médicos distintos.
- **FR-016**: Cuando el cambio se rechace, el sistema MUST informar de todas las reglas incumplidas, indicando para cada una el médico y la fecha afectados.
- **FR-017**: Al elegir receptor, el sistema SHOULD ofrecer solo médicos que cumplan FR-010 a FR-015, sin que esto sustituya la validación de FR-001 a FR-015 en el servidor.

**Efecto en el calendario**

- **FR-018**: En un CAMBIO, el sistema MUST intercambiar los médicos de ambas guardias; el tipo de guardia y el festivo se mantienen con cada día (CG-017).
- **FR-019**: En una CESION, la guardia de la fecha origen MUST pasar al receptor con el mismo tipo y festivo, y el cedente MUST dejar de tenerla (CG-018).
- **FR-020**: Si tras el cambio un día pasa a tener o deja de tener un residente R3, R4 o R5, el sistema MUST cambiar las guardias de adjunto de ese día de refuerzo a localizada (si entra) o de localizada a refuerzo (si sale), con independencia de si el residente es el solicitante o el receptor (CG-019).
- **FR-021**: El registro del cambio y todas sus modificaciones del calendario (incluido FR-020) MUST guardarse de forma atómica: o se aplica todo o nada (CG-021; principio VII).
- **FR-022**: Las reglas MUST evaluarse una sola vez, en el momento del registro (CG-021).

**Equilibrio del reparto**

- **FR-023**: Cada cesión MUST guardar el tipo y el festivo de la guardia cedida tal como eran antes de la cesión, para que el equilibrio del reparto pueda contarla para el cedente con esos valores (CG-020). Este incremento MUST NOT modificar el cálculo de equilibrio del generador de agenda del legacy; la corrección del cálculo forma parte de la migración del generador.

**Datos**

- **FR-024**: El sistema MUST guardar el histórico de tipo de cada médico (adjunto o residente, y subtipo de residente) con sus fechas de vigencia, para evaluar FR-013 y FR-020 en la fecha de cada guardia (CG-014).
- **FR-025**: El sistema MUST impedir, también a nivel de datos, que un médico tenga más de una guardia el mismo día en el mismo servicio (CG-011).
- **FR-026**: Mientras convivan legacy y sistema nuevo, los cambios registrados MUST ser visibles en el calendario del legacy (constitución, restricciones técnicas: el esquema legacy es la fuente de verdad durante la convivencia).

**Notificaciones y corrección**

- **FR-027**: Esta funcionalidad MUST NOT enviar notificaciones de los cambios registrados (CG-022: el correo del legacy hoy no funciona; las notificaciones podrán añadirse en otra funcionalidad).
- **FR-028**: Un cambio registrado por error MUST corregirse registrando el cambio inverso como un cambio normal, sujeto a todas las reglas. No existe acción de deshacer ni de anular.

**Seguridad del acceso**

- **FR-029**: Tras 5 intentos de inicio de sesión fallidos consecutivos sobre una misma cuenta, el sistema MUST bloquear el acceso de esa cuenta a la aplicación nueva durante 15 minutos, sin revelar si la cuenta existe; un inicio de sesión correcto reinicia el contador. El login del legacy no se ve afectado.

**Interfaz: capacidades del legacy (constitución, principio VIII; decisiones H1–H5 del plan, 2026-10-25)**

- **FR-030**: El calendario en el que el administrador elige la guardia MUST ofrecer las vistas de año, mes, semana y día del calendario legacy, con navegación al periodo anterior y siguiente, vuelta a hoy, paso de un día a su vista de día y acceso a todas las guardias de los días con muchas (H1).
- **FR-031**: El calendario MUST distinguir el tipo de cada guardia (presencia, localizada, refuerzo, residente) y si es festiva con una leyenda, sin depender solo del color, y MUST ordenar las guardias de cada día como el legacy: presencia, residente, y después localizada y refuerzo (H3).
- **FR-032**: Como en el legacy, el administrador MUST poder arrastrar una guardia futura a otro día futuro del calendario para proponer el intercambio con ese día. Se abre un popup con la fecha de inicio (la guardia arrastrada) y la de fin (el día donde se suelta), y los médicos candidatos de ese día. Arrastrar no aplica nada: el cambio se registra al confirmar en el popup y el servidor valida todas las reglas. Debe existir una alternativa sin arrastrar (pulsar o teclado → popup → Ceder o Intercambiar, con la fecha de fin editable) (H2, decisión del responsable del 2026-10-25, que sustituye a la anterior).
- **Decisiones aprobadas sobre capacidades** (no son pérdidas, salvo donde se dice):
  - H2: la bandeja externa del legacy, que servía para llevar una guardia a otro mes, se sustituye por dos vías: arrastrar en la vista de año, o editar la fecha de fin en el popup.
  - H4: el listado de cambios es mensual, del más reciente al más antiguo, sin búsqueda ni ordenación por columna.
  - H5: los médicos sin rol de administrador no ven sus cambios en la aplicación nueva. Los siguen viendo en el legacy (fuera de alcance, CG-002).

### Key Entities *(include if feature involves data)*

- **Guardia**: asignación de un médico a un día en un servicio, con un tipo (presencia, localizada, refuerzo o guardia de residente) y la marca de festivo. Un médico tiene como máximo una guardia por día y servicio.
- **Cambio de guardia**: registro inmutable de un CAMBIO o una CESION aplicados: tipo, fecha origen y, en un CAMBIO, fecha destino; cedente y receptor; tipo y festivo de cada guardia afectada antes del cambio; servicio; administrador y momento del registro.
- **Médico**: persona que hace guardias; puede pertenecer a varios servicios; tiene estado activo/inactivo.
- **Tipo de médico (histórico)**: tipo (adjunto, residente) y subtipo de residente (R1–R5, simulado, rotante) de un médico entre dos fechas.
- **Vacaciones**: días de vacaciones de un médico, comunes a todos sus servicios.
- **Servicio**: unidad organizativa con su calendario de guardias, sus médicos y sus administradores.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un administrador registra un cambio o una cesión válidos en menos de 1 minuto desde que abre el calendario.
- **SC-002**: El 100 % de los casos de prueba que incumplen alguna regla (FR-005 a FR-015) se rechazan, también cuando se intentan sin pasar por la pantalla, y el calendario no cambia.
- **SC-003**: En el 100 % de los rechazos, el mensaje identifica todas las reglas incumplidas con el médico y la fecha afectados, de forma comprensible para el administrador sin ayuda técnica.
- **SC-004**: Ningún cambio queda registrado sin su efecto completo en el calendario: 0 casos en las pruebas de fallo a mitad de operación y de cambios simultáneos.
- **SC-005**: Tras cada cambio aceptado, ningún médico tiene dos guardias el mismo día en el mismo servicio (0 casos en las pruebas).
- **SC-006**: El 100 % de las cesiones registradas guardan el tipo y el festivo de la guardia cedida, y el cálculo de equilibrio del legacy da los mismos resultados antes y después de desplegar la funcionalidad para un mismo conjunto de datos.
- **SC-007**: Un usuario sin rol de administrador no consigue registrar ningún cambio (0 casos en las pruebas de permisos).
- **SC-008**: Los cambios registrados se ven en el calendario del legacy el mismo día del registro, mientras ambos sistemas convivan.

## Assumptions

- **Usuarios**: los administradores de cada servicio (hoy, dos en el servicio principal) registran los cambios ya acordados con los médicos; los médicos no usan esta funcionalidad (CG-002).
- **Alcance excluido**: solicitud de cambios por el médico, aprobación, rechazo y cancelación (CG-003, CG-004), sincronización con Google Calendar (CG-023), interruptor por servicio (CG-001), notificaciones (CG-022), acción de deshacer, corrección del cálculo de equilibrio del legacy (CG-020, llega con la migración del generador) y migración de la pantalla del calendario completo.
- **Corrección por cambio inverso**: con la fórmula del equilibrio (CG-020), una cesión y su inversa se compensan: el médico que acaba haciendo la guardia la suma una vez y el otro no la suma.
- **Duplicados (CG-016)**: no se define una regla específica; un cambio repetido se rechaza por FR-005 o FR-010, y la idempotencia técnica la cubre la constitución (principio VII). Pendiente de confirmación por negocio.
- **Histórico de tipo**: al ponerse en marcha, el histórico de cada médico empieza con su tipo actual; los cambios anteriores no se pueden reconstruir.
- **Convivencia**: la pantalla de cambios del legacy se deja de usar cuando esta funcionalidad esté disponible; el resto del legacy sigue leyendo el mismo calendario.
- **Limpieza de datos**: antes de imponer FR-025 hay que resolver los 3 casos actuales de médicos con dos guardias el mismo día (discovery, D9).
- **Desviaciones intencionadas respecto al legacy**: FR-001/CG-002, FR-002/CG-003, FR-004/CG-001, FR-007/CG-007, FR-009/CG-009, FR-010 en cesiones/CG-011, FR-012/CG-013 (sin exención de simulados), FR-013/CG-014, FR-020 ampliada/CG-019, FR-016 (todos los errores) y FR-027 (sin notificaciones). En la interfaz, la bandeja de H2, H4 y H5 (ver «Interfaz: capacidades del legacy»). El cálculo del equilibrio no cambia en este incremento (FR-023).
- **Rendimiento**: volumen bajo (unos 550–700 cambios al año en el servicio principal); se asumen tiempos de respuesta estándar de una aplicación web.
