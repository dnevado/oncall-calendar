# Reglas candidatas — Vacaciones de los médicos

- **Fecha:** 2026-10-25
- **Método:** skill `business-rule-mining` sobre el [descubrimiento legacy](../legacy/vacaciones-discovery.md) y las respuestas de negocio del 2026-10-25 (§ Respuestas).
- **Catálogo existente:** [reglas de cambios de guardia](cambio-guardia-reglas-candidatas.md). Coincidencias y tensiones marcadas en cada regla.
- **Validación:** respuestas de David (Administrador) del 2026-10-25. Todas las reglas validadas.
- **Siguiente paso:** especificar la funcionalidad 002 (`/speckit-specify`).

Convenciones (estados, paridad, ⚠, criticidad y confianza) iguales que en el [catálogo de cambios](cambio-guardia-reglas-candidatas.md#estados-y-convenciones).

## Respuestas de negocio (2026-10-25)

| # | Pregunta (discovery §7) | Respuesta |
|---|---|---|
| R-a | ¿Quién gestiona las vacaciones? | «El adm y se revisan en servidor» |
| R-b | ¿Hace falta aprobación? | «No aprobación» |
| R-c | Ventana de fechas / rangos | «Creo que no hay rangos cerrados» — **interpretación pendiente de confirmar** (VAC-004, VAC-005) |
| R-d | Servicio en el conflicto con guardias | «Ahora mismo uso uno por defecto; pensé en abrirlo para distintos servicios, pero no se usa. Debería mirar el servicio específico» |
| P1 | ¿Conflicto solo en el servicio? | a) Solo el servicio del administrador |
| P2 | «No hay rangos cerrados» | a) Sin límite de fechas (acotado hacia atrás por P4: sin límite hacia el futuro) |
| P3 | Guardar solo afecta a los días tocados | Sí |
| P4 | Días pasados | c) No se pueden añadir ni quitar |
| P5 | Quién ve las vacaciones | a) Solo el administrador |
| P6 | Agenda y meses vacacionales fuera de 002 | No; precisado en P9 |
| P9 | Qué parte de la agenda entra en 002 | c) No se migra; 002 garantiza que la agenda legacy sigue respetando las vacaciones |
| P7 | 5 días que coinciden con una guardia | a) Nada; se muestran como conflicto |
| P8 | Notificar por correo | No |

## Resumen

| ID | Regla | Crit. | Paridad | Estado |
|---|---|---|---|---|
| BR-CAND-VAC-001 | Solo el administrador del servicio gestiona vacaciones, comprobado en el servidor | alta | cambia | validada |
| BR-CAND-VAC-002 | Alta y baja directas, sin aprobación | media | mantiene | validada |
| BR-CAND-VAC-003 | No se marca de vacaciones un día con guardia del médico en el servicio | alta | mantiene | validada |
| BR-CAND-VAC-004 | Sin límite de fechas hacia el futuro | media | cambia | validada |
| BR-CAND-VAC-005 | Las vacaciones son días, uno por médico y fecha | media | mantiene | validada |
| BR-CAND-VAC-006 | Las vacaciones son del médico, no del servicio | alta | mantiene | validada (vía CG-012) |
| BR-CAND-VAC-007 | Guardar vacaciones solo afecta a los días añadidos o quitados | alta | cambia | validada |
| BR-CAND-VAC-008 | No se añaden ni quitan días anteriores a hoy | media | cambia | validada |
| BR-CAND-VAC-009 | Solo el administrador ve las vacaciones, con los conflictos señalados | media | cambia | validada |
| BR-CAND-VAC-010 | Quien está de vacaciones no recibe guardias en la agenda | alta | mantiene | validada (se garantiza, no se migra) |
| BR-CAND-VAC-011 | Meses vacacionales: prioridad por días de vacaciones | media | mantiene | validada (se garantiza, no se migra) |
| BR-CAND-VAC-012 | Sin notificaciones de vacaciones | baja | mantiene | validada |

## Implementación (especificación 002)

Dónde se cumple cada regla en el sistema nuevo y qué prueba lo demuestra. Rutas de código bajo `backend/src/main/java/es/medoncalls/guardias/vacaciones/` y de pruebas bajo `backend/src/test/java/es/medoncalls/guardias/`, salvo que se indique otra.

| Regla | Código | Pruebas |
|---|---|---|
| VAC-001 | `api/VacacionesController.java` (`ServicioAccess.exigirAdministrador` en las dos operaciones) | `integration/ServicioAccessCoverageTest.java`, `integration/ConsultarVacacionesIT.java`, `integration/ModificarVacacionesIT.java` (403 y 404) |
| VAC-002 | `application/ModificarVacacionesService.java`: escribe en el acto, sin estados | `integration/ModificarVacacionesIT.java` |
| VAC-003 | `domain/ReglasVacaciones.java`; guardias del servicio con `GuardiaMapper.selectByMedicosFechasServicio` tras `lockServicio` | `vacaciones/domain/ReglasVacacionesTest.java`; `ModificarVacacionesIT` (conflicto, otro servicio, concurrencia con una cesión) |
| VAC-004 | Sin comprobación de límite futuro | `ReglasVacacionesTest`, `ModificarVacacionesIT` (escenario 4) |
| VAC-005 | Clave `IND_VACACIONES_01` e `INSERT … ON DUPLICATE KEY UPDATE` (`resources/mappers/VacacionMapper.xml`); selector de varios días y periodo (`frontend/src/features/vacaciones/EditorVacaciones.tsx`) | `ModificarVacacionesIT` (idempotencia), `EditorVacaciones.test.tsx` |
| VAC-006 | Sin servicio en la tabla; CG-012 de la 001 lee la misma tabla | `ModificarVacacionesIT` (escenario 11, CG-012 tras añadir) |
| VAC-007 | `domain/ModificacionVacaciones.java` y `ModificarVacacionesService` (solo días tocados, una transacción) | `ModificarVacacionesIT` (quitar solo lo indicado, mezcla con error, fallo a mitad, dos administradores) |
| VAC-008 | `domain/ReglasVacaciones.java` (añadir y quitar), «hoy» con `Clock` | `ReglasVacacionesTest`, `ModificarVacacionesIT`, `ConsultarVacacionesIT` (conflictos de 2017 no editables) |
| VAC-009 | `application/ConsultarVacacionesService.java` (conflictos por `LEFT JOIN`); `frontend/src/features/vacaciones/ResumenVacaciones.tsx` | `ConsultarVacacionesIT`, `contract/VacacionesContractTest.java`, `ResumenVacaciones.test.tsx` |
| VAC-010 | Misma tabla y formato (`yyyy-MM-dd`) | `legacy/LegacyVacacionesIT.java` (SQL original de `VacacionesDBImpl` y `NoTieneVacaciones`) |
| VAC-011 | Misma tabla y formato; la configuración sigue en el legacy | `legacy/LegacyVacacionesIT.java` (meses y recuento) |
| VAC-012 | Sin notificaciones | — (no hay código que avise) |

## Detalle

### BR-CAND-VAC-001 — Quién gestiona las vacaciones
- **Regla:** solo un administrador del servicio puede añadir o quitar días de vacaciones de un médico de ese servicio. El servidor lo comprueba en cada petición.
- **Legacy:** el botón «Vacaciones» está deshabilitado para quien no es administrador (`web/medico/detallemedico.jsp:29-35`, `:599` ⚠), pero la rama de guardado no comprueba el rol ni que el `ID` sea del servicio (`:49-250`). Cualquier usuario autenticado puede enviar vacaciones de cualquier médico (discovery R2).
- **Negocio:** R-a.
- **Catálogo:** coherente con CG-002 (solo el administrador registra cambios).
- **Paridad:** cambia (de solo cliente a servidor). **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-VAC-002 — Sin aprobación
- **Regla:** añadir o quitar días de vacaciones surte efecto en el acto; no hay estados ni aprobación.
- **Evidencia:** `detallemedico.jsp:235-246` escribe directamente en `vacaciones_medicos`; la tabla no tiene estado.
- **Negocio:** R-b.
- **Catálogo:** análogo a CG-003.
- **Paridad:** mantiene. **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-VAC-003 — Vacaciones y guardias del mismo día
- **Regla:** no se puede marcar de vacaciones un día en que el médico tiene una guardia en el servicio. Se rechaza la operación indicando la fecha en conflicto.
- **Evidencia:** `detallemedico.jsp:143-165` (`GuardiasDBImpl.getGuardiasMedicoFecha(ID, día, servicioSesión)`); mensaje «Existe el día de vacaciones X con guardia activa». Capa: servidor.
- **Negocio:** R-d: el conflicto se mira en el servicio específico.
- **Datos:** 5 días de vacaciones coinciden hoy con una guardia del mismo médico (copia local; discovery §4).
- **Relación con el catálogo:** CG-012 bloquea recibir guardias en **cualquier** servicio; esta regla solo mira el servicio del administrador (P1 a). Asimetría aceptada: con un único servicio en uso no hay diferencia práctica.
- **Datos existentes:** los 5 conflictos actuales no se corrigen; se muestran como conflicto al administrador (P7 a, ver VAC-009).
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-VAC-004 — Ventana de fechas
- **Regla:** no hay límite de fechas hacia el futuro: el administrador puede marcar o quitar vacaciones en cualquier fecha a partir de hoy (el límite hacia atrás es VAC-008).
- **Legacy:** el selector solo permite de −1 mes a +12 meses (`detallemedico.jsp:372` ⚠); el servidor no lo comprueba. La ficha carga de día 1 del mes anterior a +2 años (`:291-298`).
- **Negocio:** R-c; P2 a. Se combina con P4 c: sin límite hacia el futuro, ninguno hacia el pasado.
- **Paridad:** cambia. **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-VAC-005 — Días sueltos
- **Regla:** las vacaciones se registran por días; un médico tiene como máximo un registro por fecha. Elegir varios días a la vez es una capacidad de la interfaz que se conserva (CLAUDE.md); la interfaz puede además ofrecer elegir periodos, que se guardan como días.
- **Evidencia:** MultiDatesPicker de días sueltos (`detallemedico.jsp:372-383`); `UNIQUE KEY IND_VACACIONES_01 (IdMedico, FDay)`.
- **Negocio:** P2 a (R-c se refería a la ventana de fechas, no a los periodos).
- **Paridad:** mantiene. **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-VAC-006 — Vacaciones del médico
- **Regla:** un día de vacaciones pertenece al médico y le afecta en todos sus servicios.
- **Evidencia:** `vacaciones_medicos` no tiene `IdServicio`; CG-012 ya migrada («en cualquier servicio», `backend/.../ReglasCambio.java:98`).
- **Negocio:** P9 del cuestionario de cambios («Sí, son globales»); R-d confirma que el multiservicio no se usa.
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada (vía CG-012).

### BR-CAND-VAC-007 — Guardar no borra lo que no se toca
- **Regla:** añadir o quitar días solo afecta a esos días; las demás vacaciones del médico quedan intactas. La operación es atómica: o se aplica entera o no se aplica.
- **Legacy:** guardar la ficha borra todo desde `min(día 1 del mes actual, día elegido más antiguo)` y vuelve a insertar lo recibido (`detallemedico.jsp:211-246`), sin transacción y con errores de alta ignorados (`VacacionesDBImpl.java:31-60`, `:127`). Si no se abrió el selector, borra todas las vacaciones futuras (discovery R1, inferido).
- **Negocio:** P3 «Sí».
- **Paridad:** cambia (corrige R1 y R3). **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-VAC-008 — Vacaciones pasadas
- **Regla:** no se pueden añadir ni quitar días de vacaciones anteriores a hoy. Hoy sí se admite (no es un día pasado).
- **Legacy:** el selector permite desde 1 mes atrás (`detallemedico.jsp:372` ⚠) y el borrado alcanza como mucho el día seleccionado más antiguo (`:211-235`).
- **Negocio:** P4 c.
- **Paridad:** cambia. **Criticidad / confianza:** media / alta. **Estado:** validada.
- **Supuesto a confirmar en la especificación:** que «hoy» cuenta como no pasado.

### BR-CAND-VAC-009 — Consulta
- **Regla:** solo el administrador del servicio ve las vacaciones de los médicos del servicio. Los días de vacaciones que coinciden con una guardia del médico en el servicio se le muestran señalados como conflicto.
- **Legacy:** el administrador ve los días en la ficha (solo tras abrir el selector) y los meses con vacaciones de los próximos 6 meses en el listado (`lista_medicos.jsp:98-157`). El médico abre su ficha pero no puede abrir el selector (`:599`), así que en la práctica no ve sus vacaciones. El calendario no las muestra (discovery R10).
- **Negocio:** P5 a; P7 a.
- **Paridad:** cambia (señalar conflictos es nuevo). **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-VAC-010 — Agenda (consumidor)
- **Regla:** al generar o rellenar la agenda, no se asigna guardia a quien está de vacaciones ese día; y sus días de vacaciones se descuentan de la capacidad del mes.
- **Evidencia:** `ProcesarMedicos.NoTieneVacaciones` (`ProcesarMedicos.java:2505-2518`) y usos en `calc_agenda.jsp:525`, `:856`; `fill_agenda.jsp:128`; capacidad en `calc_agenda.jsp:206-249`.
- **Alcance:** P9 c: la agenda no se migra en 002, pero 002 debe garantizar que la regla se sigue cumpliendo. Las vacaciones que escribe la nueva aplicación usan la misma tabla y el mismo formato (`yyyy-MM-dd`), y se comprueba con una prueba que la agenda legacy las respeta.
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-VAC-011 — Meses vacacionales (consumidor)
- **Regla:** en los meses configurados por servicio (`AJUSTE_A_ESTOS_MESES_VACACIONES`, hoy `7|8|9`), la asignación de la agenda prioriza a quien más días de vacaciones tiene ese mes, y el equilibrio histórico solo cuenta esos meses.
- **Evidencia:** `Util.java:456-488`; `ProcesarMedicos.java:2438-2500`; `ByVacacionesDESCResidentes.java:11`; `GuardiasDBImpl.java:377-395`; configuración en `config_form.jsp:193-195`, `:443-460` (con el defecto R8).
- **Alcance:** P9 c: la agenda no se migra en 002, pero 002 debe garantizar que la regla se sigue cumpliendo. Las vacaciones que escribe la nueva aplicación usan la misma tabla y el mismo formato (`yyyy-MM-dd`), y se comprueba con una prueba que la agenda legacy las respeta.
- **Paridad:** mantiene. **Criticidad / confianza:** media / media. **Estado:** validada.

### BR-CAND-VAC-012 — Sin notificaciones
- **Regla:** añadir o quitar vacaciones no envía avisos.
- **Evidencia:** el guardado (`detallemedico.jsp:205-248`) no envía correo, a diferencia de los cambios (`add_cambio_guardia.jsp:301-312`).
- **Negocio:** P8 «No».
- **Paridad:** mantiene. **Criticidad / confianza:** baja / alta. **Estado:** validada.

## Riesgos de capa

- ⚠ **Solo en el frontend:** permiso de gestión (VAC-001) y ventana de fechas (VAC-004). En el sistema nuevo el permiso se comprueba en el servidor (principio II).
- La pérdida de vacaciones al guardar la ficha (discovery R1) seguirá ocurriendo en el legacy mientras convivan ambas aplicaciones sobre la misma tabla.

## Preguntas pendientes para negocio

Ninguna: P1–P9 respondidas (tabla de respuestas).
