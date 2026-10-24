# Reglas candidatas — Cambios y cesiones de guardia

- **Fecha:** 2026-10-24
- **Método:** skill `business-rule-mining` sobre el [discovery legacy](../legacy/cambio-guardia-discovery.md) y las respuestas del [cuestionario de negocio](../legacy/cambio-guardia-cuestionario-negocio.md).
- **Catálogo existente:** no existe; todas las candidatas son **nuevas**.
- **Validación:** firmada por David (Administrador) el 2026-10-24. La firma cubre todas las reglas salvo CG-016. CG-022 queda pendiente por su respuesta.
- **Siguiente paso:** incorporar las reglas `validada` al catálogo versionado (constitución, principio I).

## Estados y convenciones

| Estado | Significado |
|---|---|
| `validada` | Negocio la confirma; lista para el catálogo versionado |
| `descartada` | Negocio decide que no forma parte del sistema nuevo |
| `pendiente de validación` | Falta una decisión de negocio |

- **Paridad:** `mantiene` = el sistema nuevo se comporta como el legacy; `cambia` = desviación intencionada que la especificación debe declarar.
- **Capa legacy:** dónde se aplica hoy. ⚠ = solo en el frontend (riesgo, constitución principio II).
- **Criticidad:** alta = afecta a la integridad del calendario, a permisos o al reparto; media = afecta al flujo o a la experiencia; baja = accesoria.
- **Confianza:** en la interpretación del comportamiento legacy a partir de la evidencia (alta/media/baja).
- Los recuentos de datos son orientativos: hubo inserciones manuales sin trazabilidad (discovery §8).

## Resumen

| ID | Regla | Crit. | Paridad | Estado |
|---|---|---|---|---|
| BR-CAND-CG-001 | Interruptor de cambios por servicio | media | cambia (se elimina) | descartada |
| BR-CAND-CG-002 | Solo un administrador registra cambios | alta | cambia | validada |
| BR-CAND-CG-003 | El cambio se registra y se aplica en el acto, sin estados intermedios | alta | cambia | validada |
| BR-CAND-CG-004 | Transición de estado única desde PENDIENTE | alta | — | descartada (sin estados, por CG-003) |
| BR-CAND-CG-005 | El cedente debe tener la guardia en la fecha origen | alta | mantiene | validada |
| BR-CAND-CG-006 | En un CAMBIO, el receptor debe tener guardia en la fecha destino | alta | mantiene | validada |
| BR-CAND-CG-007 | En una CESION, el receptor no necesita guardia en otro día | media | cambia | validada |
| BR-CAND-CG-008 | No se cede ni se cambia una guardia del mismo día ni pasada | alta | mantiene | validada |
| BR-CAND-CG-009 | En un CAMBIO no se recibe una guardia de fecha pasada | alta | cambia | validada |
| BR-CAND-CG-010 | No se exige antelación mínima | baja | mantiene | validada |
| BR-CAND-CG-011 | Nadie recibe una guardia en un día en que ya tiene guardia | alta | cambia (cesión) | validada |
| BR-CAND-CG-012 | Nadie recibe una guardia en un día de vacaciones; las vacaciones son globales | alta | mantiene | validada |
| BR-CAND-CG-013 | Nadie queda con presencias en días consecutivos; sin exención de simulados | alta | cambia | validada |
| BR-CAND-CG-014 | Mismo tipo de médico, según el tipo en la fecha del cambio | alta | cambia | validada |
| BR-CAND-CG-015 | El receptor debe estar activo (en general) | media | mantiene (⚠ hoy solo frontend) | validada |
| BR-CAND-CG-016 | No se registran cambios duplicados | media | — | pendiente de validación |
| BR-CAND-CG-017 | Un CAMBIO intercambia médicos; tipo y festivo se quedan con el día | alta | mantiene | validada |
| BR-CAND-CG-018 | Una CESION pasa la guardia al receptor con el mismo tipo y festivo | alta | mantiene | validada |
| BR-CAND-CG-019 | Ajuste del adjunto según haya o no R3–R5 ese día | alta | mantiene (ampliada) | validada |
| BR-CAND-CG-020 | En el equilibrio del reparto, la cesión cuenta para el cedente (salvo meses vacacionales) | alta | mantiene (y corrige) | validada |
| BR-CAND-CG-021 | Las reglas se evalúan una sola vez, al registrar | media | mantiene | validada |
| BR-CAND-CG-022 | Notificación por correo | media | — | pendiente de validación |
| BR-CAND-CG-023 | Sincronización con Google Calendar | baja | cambia (se elimina) | descartada (fuera de alcance) |

**Totales:** 18 validadas, 3 descartadas, 2 pendientes.

---

## Detalle

### BR-CAND-CG-001 — Interruptor de cambios por servicio
- **Legacy:** si el servicio no tiene activados los cambios (`ACTIVAR_CAMBIO_GUARDIAS = 'S'`), no se puede registrar ninguno. Evidencia: `web/medico/cambio_guardia.jsp:72`, `:144`; `web/config/config_form.jsp:161-163`. Solo en la prevalidación.
- **Negocio:** no se mantiene el interruptor.
- **Decisión:** los cambios de guardia están siempre disponibles; la clave de configuración no se migra.
- **Estado:** descartada.

### BR-CAND-CG-002 — Quién registra cambios
- **Regla:** solo un administrador del servicio registra cambios y cesiones, en nombre de los médicos implicados.
- **Legacy:** cualquier médico puede solicitar cambios de sus guardias. Arrastrar guardias ajenas se impide solo en el cliente (`web/inicio.jsp:387` ⚠) y el servidor acepta el solicitante del cliente sin compararlo con la sesión (`add_cambio_guardia.jsp:47`).
- **Negocio:** P6-d «Solo registro por el administrador».
- **Datos:** el 99,8 % de los cambios se aprobó el mismo día que se registró, siempre por uno de los dos administradores.
- **Paridad:** cambia. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-003 — Registro y aplicación en el acto
- **Regla:** un cambio registrado por el administrador se aplica inmediatamente al calendario. No existen los estados PENDIENTE ni RECHAZADA; el receptor no tiene que aceptarlo y el solicitante no puede cancelarlo.
- **Legacy:** circuito PENDIENTE → APROBADA/CANCELADA, con autoaprobación del administrador (`add_cambio_guardia.jsp:58-59`; `web/js/guardias.js:518-526`). El indicador `VALIDAR_CAMBIOS_GUARDIAS_BY_ADM` no tiene efecto (`cambio_guardia.jsp:74`). RECHAZADA nunca se ofrece en la interfaz (`lista_cambiosguardias.jsp:98-99`).
- **Negocio:** P6-a «Siempre», P6-b «No», P6-c «No»; pregunta abierta 2: no hacen falta PENDIENTE ni RECHAZADA.
- **Paridad:** cambia. **Criticidad / confianza:** alta / alta. **Estado:** validada.
- **Pendiente:** cómo se corrige un cambio registrado por error (ver preguntas abiertas).

### BR-CAND-CG-004 — Transición de estado única
- **Legacy:** solo la interfaz impide aprobar un cambio que no está PENDIENTE (`lista_cambiosguardias.jsp:94` ⚠); el servidor lo volvería a aplicar (`cambio_estado_guardia.jsp:77-94`, `:115`).
- **Decisión:** sin estados intermedios (CG-003), la regla no aplica. El requisito técnico equivalente queda cubierto por la constitución (principio VII): cada cambio se aplica una sola vez, de forma atómica e idempotente.
- **Estado:** descartada.

### BR-CAND-CG-005 — El cedente tiene la guardia
- **Regla:** la guardia que se cede o se cambia debe estar asignada al cedente en la fecha origen.
- **Evidencia:** `cambio_guardia.jsp:150` (solo si no es administrador); `add_cambio_guardia.jsp:62`, `:71-72`, `:292`; `cambio_estado_guardia.jsp:118-123`.
- **Capa legacy:** backend, en el alta y al aplicar.
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-006 — Receptor con guardia en destino (CAMBIO)
- **Regla:** en un CAMBIO, el receptor debe tener guardia en la fecha destino, porque es la que entrega a cambio.
- **Evidencia:** `add_cambio_guardia.jsp:63`, `:73-74`, `:292`; `cambio_estado_guardia.jsp:124-127`.
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-007 — Receptor de una cesión
- **Regla:** en una CESION solo intervienen la fecha origen y el receptor; no se exige que el receptor tenga guardia en otro día. El cambio guarda el tipo y el festivo de la guardia cedida.
- **Legacy:** el receptor solo puede elegirse entre quienes tienen guardia en la fecha destino (`_list_medicos_cambio_guardia.jsp:72`, `:137-158`; `add_cambio_guardia.jsp:63`, `:292`), y esa guardia no se modifica (`cambio_estado_guardia.jsp:306-348`).
- **Negocio:** P2 (la exigencia se debe a que CAMBIO y CESION comparten formulario).
- **Paridad:** cambia. **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-CG-008 — Sin cambios del mismo día ni pasados
- **Regla:** la guardia que se cede o se cambia debe ser de una fecha posterior a hoy.
- **Evidencia:** `cambio_guardia.jsp:136`, `:177`; solo en la prevalidación ⚠ (no se repite en `add_cambio_guardia.jsp`).
- **Negocio:** P3-a «No» al mismo día.
- **Paridad:** mantiene (y se valida en el servidor en el registro). **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-009 — No recibir guardias pasadas (CAMBIO)
- **Regla:** en un CAMBIO, la fecha destino no puede ser anterior a hoy. No aplica a la CESION, que no tiene fecha destino (CG-007).
- **Legacy:** se permite a propósito: «LO QUITO, LAS CESIONES PUEDEN SER ANTERIORES» (`cambio_guardia.jsp:137-139`).
- **Negocio:** P3-b «No»; pregunta abierta 3: solo al CAMBIO.
- **Paridad:** cambia. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-010 — Sin antelación mínima
- **Regla:** basta con que la guardia sea de un día posterior a hoy (CG-008).
- **Evidencia:** no existe ninguna comprobación de antelación en el flujo.
- **Negocio:** P3-c «No».
- **Paridad:** mantiene. **Criticidad / confianza:** baja / alta. **Estado:** validada.

### BR-CAND-CG-011 — Sin doble guardia el mismo día
- **Regla:** nadie puede recibir una guardia en un día en que ya tiene otra.
- **Legacy:** en el CAMBIO se comprueba para ambos (`add_cambio_guardia.jsp:92-104`). En la CESION se calcula pero no se evalúa (`add_cambio_guardia.jsp:204`).
- **Negocio:** P1 «Prohibir siempre».
- **Datos:** 6 cesiones con el receptor con más de una guardia ese día; 3 médicos con dos guardias el mismo día.
- **Paridad:** mantiene en CAMBIO, cambia en CESION. **Criticidad / confianza:** alta / alta. **Estado:** validada.
- **Nota técnica:** reforzar con unicidad `(médico, fecha, servicio)` en base de datos (principio VII), tras limpiar los 3 casos existentes.

### BR-CAND-CG-012 — Vacaciones
- **Regla:** nadie puede recibir una guardia en un día de vacaciones. Las vacaciones son del médico, comunes a todos sus servicios.
- **Evidencia:** `cambio_guardia.jsp:134`, `:172`; `add_cambio_guardia.jsp:106-133`, `:207-219`. Esquema: `vacaciones_medicos (IdMedico, FDay)` sin servicio.
- **Negocio:** P9 «Sí, son globales».
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-013 — Presencias consecutivas
- **Regla:** tras el cambio, nadie puede tener guardia de presencia el día anterior o el posterior a una guardia de presencia que recibe. No hay exención para residentes simulados. No se comprueba el día adyacente cuando coincide con la otra fecha del propio cambio. Solo aplica a presencias.
- **Legacy:** `add_cambio_guardia.jsp:136-188` (CAMBIO, solicitante) y `:226-284` (siempre, receptor); exime al residente simulado solicitante.
- **Negocio:** P4-a «Sí», P4-b «A ninguno», P4-c «No»; pregunta abierta 4: se mantiene la excepción del día adyacente.
- **Paridad:** cambia (sin exención de simulados). **Criticidad / confianza:** alta / media. **Estado:** validada.

### BR-CAND-CG-014 — Mismo tipo de médico
- **Regla:** cedente y receptor deben ser del mismo tipo (adjunto con adjunto, residente con residente), según el tipo que tenía cada uno **en la fecha del cambio**. Aplica también al administrador.
- **Legacy:** solo en la interfaz y solo para no administradores (`_list_medicos_cambio_guardia.jsp:150`, `:176-196` ⚠).
- **Negocio:** P5 «Regla obligatoria»; pregunta abierta 5: se evalúa en la fecha del cambio.
- **Datos:** 627 cambios entre residente y adjunto (2017–2023), probablemente por el cambio de tipo con el tiempo.
- **Paridad:** cambia. **Criticidad / confianza:** alta / alta. **Estado:** validada.
- **Requisito de datos:** el sistema nuevo DEBE guardar el histórico de tipo de cada médico (`medicos.Tipo` solo guarda el actual). Para los cambios históricos no hay forma de reconstruirlo.

### BR-CAND-CG-015 — Receptor activo
- **Regla:** solo puede recibir una guardia un médico activo en general (`medicos.Activo`) que pertenezca al servicio (dado de alta en `guardias_servicios_medicos`). No se exige `ActivoServicio`. Confirmado por negocio el 2026-10-24 durante `/speckit-analyze`.
- **Evidencia:** solo en la interfaz (`_list_medicos_cambio_guardia.jsp:145`, `:150` ⚠).
- **Negocio:** pregunta abierta 6: «General».
- **Paridad:** mantiene (pasa al servidor). **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-CG-016 — Sin duplicados
- **Legacy:** solo se impide una solicitud PENDIENTE duplicada, en la prevalidación y con el servicio fijado a 1 por defecto (`cambio_guardia.jsp:199-204`; `CambiosGuardias.java:23`).
- **Datos:** 44 combinaciones repetidas.
- **Negocio:** «No sé, puede que no aplique».
- **Propuesta:** no definirla como regla de negocio. CG-005 y CG-011 ya impiden aplicar dos veces el mismo cambio (el cedente deja de tener la guardia), y la idempotencia técnica la cubre el principio VII.
- **Estado:** pendiente de validación.

### BR-CAND-CG-017 — Efecto de un CAMBIO
- **Regla:** al aplicarse un CAMBIO, los médicos intercambian los días; el tipo de guardia y el festivo pertenecen al día, no al médico.
- **Evidencia:** `cambio_estado_guardia.jsp:164-176`, `:242-245`, `:205`, `:275`.
- **Negocio:** pregunta abierta 8: «Sí».
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-018 — Efecto de una CESION
- **Regla:** al aplicarse una CESION, la guardia de la fecha origen pasa al receptor con el mismo tipo y festivo; el cedente deja de tenerla.
- **Evidencia:** `cambio_estado_guardia.jsp:306-348`.
- **Paridad:** mantiene. **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-019 — Ajuste del adjunto por residentes R3–R5
- **Regla:** cuando un cambio hace que un día pase a tener o deje de tener un residente R3, R4 o R5, las guardias de adjunto de ese día pasan de refuerzo a localizada (si entra un R3–R5) o de localizada a refuerzo (si sale). Aplica tanto si el residente es el solicitante como si es el receptor.
- **Legacy:** `cambio_estado_guardia.jsp:144-160`, `:206-226`, `:276-300`, `:350-374`; solo se evalúa si el solicitante es RESIDENTE. R3 añadido en `181dea4` y `ec7d836`.
- **Negocio:** P8-a «Sí», P8-b «Sí»; pregunta abierta 9: también si el residente es el receptor.
- **Paridad:** mantiene (ampliada). Con CG-014 (mismo tipo), si uno es residente el otro también lo es, así que en la práctica el resultado coincide con el legacy. **Criticidad / confianza:** alta / media. **Estado:** validada.

### BR-CAND-CG-020 — Cesiones en el equilibrio del reparto
- **Regla:** en el equilibrio de guardias del generador de agenda, la guardia cedida cuenta para quien la tenía asignada (el cedente), con su tipo y su festivo originales. El ajuste no se aplica en los meses vacacionales.
- **Legacy:** intención correcta (`ProcesarMedicos.java:620-631`), implementación defectuosa: toma el tipo del día equivocado, casi nunca descuenta al receptor y mezcla festivos con no festivos (`CambiosGuardias.xml:9-44`; `ProcesarMedicos.java:628-630`). Exclusión de meses vacacionales en `:623`.
- **Negocio:** P10 «Para quien la tenía asignada»; pregunta abierta 10: la exclusión de meses vacacionales es intencionada.
- **Paridad:** mantiene la regla, corrige el cálculo; el reparto diferirá del legacy (desviación declarada). **Criticidad / confianza:** alta / alta. **Estado:** validada.

### BR-CAND-CG-021 — Evaluación única
- **Regla:** las reglas se evalúan una sola vez, al registrar el cambio. Como el registro y la aplicación son simultáneos (CG-003), no hay revalidación posterior. El cambio y su efecto en el calendario se guardan en la misma transacción (principio VII).
- **Negocio:** P7 «No revalidar».
- **Paridad:** mantiene. **Criticidad / confianza:** media / alta. **Estado:** validada.

### BR-CAND-CG-022 — Notificación por correo
- **Legacy:** al registrar y al cambiar de estado se envía un correo a los administradores del servicio, al solicitante y al receptor (`add_cambio_guardia.jsp:301-312`; `cambio_estado_guardia.jsp:394-411`; `MailingUtil.java:180-182`).
- **Negocio:** «Hoy no funciona»: el correo del legacy está desactivado o roto en producción.
- **Estado:** pendiente de validación. Falta decidir si el sistema nuevo notifica, a quién y por qué canal.

### BR-CAND-CG-023 — Google Calendar
- **Legacy:** si el servicio tiene activada la sincronización (`CALENDARIO_GMAIL = 'S'`), al aplicar el cambio se borran y se vuelven a crear los eventos de los médicos afectados (`cambio_estado_guardia.jsp:104-112`, `:138-142`, `:179-203`, `:248-272`, `:320-344`).
- **Negocio:** fuera de alcance.
- **Decisión:** no se migra; `IdEventoGCalendar` y las claves de configuración asociadas no forman parte del modelo nuevo.
- **Estado:** descartada (fuera de alcance).

---

## Reglas aplicadas solo en el frontend (riesgo)

| Regla | Evidencia | Destino en el sistema nuevo |
|---|---|---|
| Solo se arrastran guardias propias (o todas, si es administrador) | `web/inicio.jsp:387` | Desaparece con CG-002; el rol de administrador se comprueba en el servidor |
| Aprobar o cancelar solo un administrador y solo cambios pendientes | `lista_cambiosguardias.jsp:94` | Desaparece con CG-003; el rol se comprueba en el servidor |
| Receptor activo y del mismo tipo | `_list_medicos_cambio_guardia.jsp:145-150`, `:176-196` | Servidor (CG-014, CG-015) |

## Desviaciones intencionadas respecto al legacy

- **Cambian:** CG-001 (sin interruptor), CG-002, CG-003, CG-007, CG-009, CG-011 (en cesiones), CG-013, CG-014 y CG-023 (sin Google Calendar).
- **Corrigen la implementación manteniendo la intención:** CG-020 y, vía principio VII, la aplicación única y atómica que sustituye a CG-004.
- **Amplían:** CG-019 (también con el residente como receptor).

La especificación debe declararlas todas.

## Requisitos derivados para la especificación

- Guardar el histórico de tipo de cada médico (CG-014).
- Guardar en cada cesión el tipo y el festivo de la guardia cedida (CG-007, CG-020).
- Unicidad `(médico, fecha, servicio)` en las guardias, con limpieza previa (CG-011).

## Preguntas abiertas

| # | Regla | Pregunta | Respuesta (2026-10-24) |
|---|---|---|---|
| 1 | CG-001 | ¿Se mantiene el interruptor por servicio? | No |
| 2 | CG-003 | ¿Siguen haciendo falta los estados PENDIENTE y RECHAZADA? | No |
| 3 | CG-009 | ¿La regla de destino no pasado aplica solo al CAMBIO? | Sí |
| 4 | CG-013 | ¿Se mantiene la excepción del día adyacente que coincide con la otra fecha del cambio? | Sí |
| 5 | CG-014 | ¿El tipo de médico se evalúa en la fecha del cambio (requiere histórico)? | Sí |
| 6 | CG-015 | ¿Activo en el servicio o en general? | General |
| 7 | CG-016 | ¿Qué define un cambio duplicado? | No sé, puede que no aplique |
| 8 | CG-017 | Confirmar el efecto de un CAMBIO | Sí |
| 9 | CG-019 | ¿El ajuste aplica también cuando el residente es el receptor? | Sí |
| 10 | CG-020 | ¿Es intencionado que no se aplique en meses vacacionales? | Sí |
| 11 | CG-022 | Destinatarios del correo y comportamiento si falla | No hay correo (hoy no funciona) |
| 12 | CG-023 | ¿Google Calendar entra en el alcance? | No |

**Siguen abiertas:**

| # | Regla | Pregunta |
|---|---|---|
| 13 | CG-016 | ¿Se acepta la propuesta de no definir una regla de duplicados (la cubren CG-005, CG-011 y el principio VII)? |
| 14 | CG-022 | ¿El sistema nuevo notifica los cambios? Si es así, ¿a quién y por qué canal? |
| 15 | CG-003 | Sin estados ni cancelación, ¿cómo se corrige un cambio registrado por error (registrar el cambio inverso, deshacer…)? |

## Firma de validación

| Nombre | Rol | Fecha | Reglas validadas |
|---|---|---|---|
| David | Administrador | 2026-10-24 | Todas salvo CG-016 (CG-022 pendiente por su respuesta) |
