# Descubrimiento legacy — Solicitud y aprobación de cambio de guardia

- **Fecha:** 2026-10-24
- **Alcance:** solicitud de cambio/cesión de guardia (arrastrar en calendario → formulario → alta) y su aprobación.
- **Método:** skill `legacy-discovery`; solo lectura de código e historial git. No se consultó base de datos ni se ejecutaron pruebas.
- **Estado:** borrador pendiente de validación por negocio (ver [cuestionario](cambio-guardia-cuestionario-negocio.md)).

## 1. Mapa del flujo

### Paso A — Arrastrar guardia en el calendario (prevalidación)
1. FullCalendar `editable: true` (`web/inicio.jsp:306`). En `eventDragStart`, `cancelDrag = !_IsMedicoONCall(...) && !_IS_A` (`web/inicio.jsp:387`): impedir arrastrar guardias ajenas es **solo cliente**.
2. `eventDrop` → `_DropEvent` (`web/inicio.jsp:394-396`) → modal `#modalCambioGuardias` → POST `start=<fecha destino>&duration=<delta días>` a `medico/cambio_guardia.jsp` (`web/inicio.jsp:158-163`).
3. `web/medico/cambio_guardia.jsp`:
   - Origen = `start − duration`, destino = `start` (`:83-92`).
   - Lee configuración por servicio `ACTIVAR_CAMBIO_GUARDIAS` y `VALIDAR_CAMBIOS_GUARDIAS_BY_ADM` (`:72-74`).
   - Valida (V2–V7) y responde `NOOK.<mensaje>` o JSON `{medico,start,end}` (`:209-219`). **No inserta** (insert comentado en `:208`).
4. Cliente → `_newChange` (`web/inicio.jsp:182`) → carga `medico/_list_medicos_cambio_guardia.jsp?idsolicitante&inicio&fin` en diálogo (`web/js/guardias.js:541-561`).

### Paso B — Formulario y alta
5. `web/medico/_list_medicos_cambio_guardia.jsp`: selector de tipo CAMBIO/CESION y de médicos con guardia en destino (`:72`, `:129-159`). No-admin: solo activos, mismo tipo y distinto de sí mismo (`:150`). Admin: además elige solicitante entre los de guardia en origen (`:69`, `:99-119`).
6. `_saveChangeOnCall()` construye `{IdMedicoSolicitante, FechaIniCambio, FechaFinCambio, IdMedicoDestino, Estado:'PENDIENTE', TipoCambio}` (`:209-223`) → `_addChange` (`web/js/guardias.js:502-509`) → POST `datachange` a `medico/add_cambio_guardia.jsp`.
7. `web/medico/add_cambio_guardia.jsp`: Gson (`:47`), `IdCambio = max+1` (`:53`), `IdServicio` de sesión (`:56`), si admin `Estado=APROBADA` (`:58-59`); valida (V9–V14); inserta (`:299`); email a administradores del servicio + solicitante + destinatario (`:301-312`); responde JSON del cambio (`:315-318`).
8. Si admin, el cliente encadena `_changeState(...)` → `medico/cambio_estado_guardia.jsp` (`web/js/guardias.js:518-526`, `:589-599`).

### Paso C — Aprobación / cancelación
9. `web/medico/lista_cambiosguardias.jsp`: admin ve todos; no-admin ve los suyos (`:51-52`). Botones APROBAR/CANCELAR solo para admin y estado PENDIENTE (`:94-99`).
10. `_state` → (si no es CANCELADA) formulario `_list_medicos_cambio_guardia.jsp?cambioid&newstate&type` → `_changeState` → `medico/cambio_estado_guardia.jsp` (`web/js/guardias.js:563-599`).
11. `web/medico/cambio_estado_guardia.jsp`:
    - Siempre: `UPDATE guardias_cambios` (estado, fecha y usuario de aprobación, destinatario, tipo) (`:83-94`) y email de confirmación (`:394-411`).
    - Si `APROBADA`:

| Tipo | Efecto en `guardias_medicos` | Evidencia |
|---|---|---|
| CAMBIO | Intercambia médicos; tipo y festivo se quedan con el día | `:164-275` |
| CESION | Borra la guardia del solicitante en origen e inserta una igual para el destinatario | `:306-348` |
| Ajuste residentes | Si el solicitante es RESIDENTE y solo uno de los dos es R3/R4/R5, las guardias de adjunto de ese día pasan LOCALIZADA↔REFUERZO | `:144-160`, `:206-226`, `:276-300`, `:350-374` |
| Google Calendar | Si `CALENDARIO_GMAIL='S'`, borra/crea eventos y los envía | `:104-112`, `:138-142`, `:179-203` |

## 2. Artefactos

| Tipo | Ruta | Responsabilidad | Dependencias |
|---|---|---|---|
| JSP pantalla | `web/inicio.jsp` | Calendario, drag & drop, prevalidación AJAX | FullCalendar, `common/init.jsp` (`_IS_A`) |
| JSP pantalla | `web/medico/lista_cambiosguardias.jsp` | Listado y acciones aprobar/cancelar | `CambiosGuardiasDBImpl` |
| JS | `web/js/guardias.js` | `_newChange`, `_addChange`, `_state`, `_changeState` | jQuery, jQuery UI |
| JSP endpoint | `web/medico/cambio_guardia.jsp` | Prevalidación | `GuardiasDBImpl`, `VacacionesDBImpl`, `ConfigurationDBImpl`, `CambiosGuardiasDBImpl` |
| JSP fragmento | `web/medico/_list_medicos_cambio_guardia.jsp` | Formulario destinatario/tipo (alta y aprobación) | `GuardiasDBImpl`, `MedicoDBImpl` |
| JSP endpoint | `web/medico/add_cambio_guardia.jsp` | Validación final, alta y email | Gson, `MailingUtil`, DBImpl |
| JSP endpoint | `web/medico/cambio_estado_guardia.jsp` | Cambio de estado y aplicación del cambio | `GuardiasDBImpl`, `CalendarEventUtil`, `MailingUtil` |
| DTO | `src/com/guardias/cambios/CambiosGuardias.java` | Entidad del cambio (fechas `String`) | — |
| DAO iBATIS | `src/com/guardias/database/CambiosGuardiasDBImpl.java` | `guardias_cambios`; crea `SqlSessionFactory` en cada llamada (`:261-278`) | `ConfigurationIBATIS.xml` |
| Mapper | `src/com/guardias/persistence/CambiosGuardias.xml` (duplicado en `web/WEB-INF/classes/...`) | SQL de cambios | — |
| DAO JDBC | `GuardiasDBImpl`, `VacacionesDBImpl`, `ConfigurationDBImpl`, `MedicoDBImpl` | SQL concatenado con `Statement` | `ConexionGuardias` |
| Mail | `src/com/guardias/mail/MailingUtil.java:142` | FreeMarker `schedule_daychange.ftl` / `schedule_daychange_confirmation.ftl` | `MedicoDBImpl` |
| Config UI | `web/config/config_form.jsp:161-167`, `:223-226` | Edición de `ACTIVAR_CAMBIO_GUARDIAS` y `VALIDAR_CAMBIOS_GUARDIAS_BY_ADM` | `ConfigurationDBImpl` |
| Constantes | `src/com/guardias/Util.java:51,75-76,86,156-157` | Formato fecha, enums tipo/estado/guardia, claves config | — |
| i18n | `src/medoncalls.properties:19-29` | Mensajes de error (encoding roto) | — |
| Seguridad | `src/com/guardias/login/LoginFilter.java:55-68` | Solo comprueba sesión (`/*`) | — |

## 3. SQL y tablas

| Consulta | Origen | Tabla | Op. |
|---|---|---|---|
| `SELECT * FROM configuracion WHERE config_key=… AND config_IdServicio=…` | `ConfigurationDBImpl.java:118` | `configuracion` | R |
| `SELECT … FROM guardias_medicos WHERE IdServicio=… [IdMedico] [FGuardia] [Tipo]` | `GuardiasDBImpl.java:565-592` | `guardias_medicos` | R |
| `SELECT * FROM vacaciones_medicos WHERE IdMedico=… and FDay=…` | `VacacionesDBImpl.java:15,198-204` | `vacaciones_medicos` | R (sin servicio) |
| `SELECT DISTINCT medicos.* … LEFT JOIN guardias_servicios_medicos` | `MedicoDBImpl.java:384-406` | `medicos`, `guardias_servicios_medicos` | R |
| `getMaxIDCambiosGuardiasID` | `CambiosGuardias.xml:59-64` | `guardias_cambios` | R |
| `getCambioGuardiasByMedicoSolicitanteYFechaYEstado` | `CambiosGuardias.xml:81-93` | `guardias_cambios` | R |
| `getCambiosGuardiasBySolicitante` | `CambiosGuardias.xml:67-79` | `guardias_cambios` | R |
| `getCambiosGuardiasById` | `CambiosGuardias.xml:49-57` | `guardias_cambios` | R |
| `insertCambiosGuardias` | `CambiosGuardias.xml:109-118` | `guardias_cambios` | **W (INSERT)** |
| `updateCambiosGuardias` | `CambiosGuardias.xml:134-149` | `guardias_cambios` | **W (UPDATE)** |
| `UpdateGuardiasFechayCalId`, `DeleteGuardia`, `AddGuardia` | `cambio_estado_guardia.jsp:205,224,275,294,306,348,368` | `guardias_medicos` | **W** |

La solicitud solo escribe en `guardias_cambios`; `guardias_medicos` se modifica únicamente al aprobar.

## 4. Validaciones (candidatas a regla, pendientes de negocio)

| # | Validación | Capa | Evidencia |
|---|---|---|---|
| V1 | No-admin solo arrastra sus guardias | **Solo cliente** | `inicio.jsp:387` |
| V2 | Cambios activados (`ACTIVAR_CAMBIO_GUARDIAS='S'`) | Servidor, solo paso A | `cambio_guardia.jsp:144` |
| V3 | No-admin debe tener guardia en origen | Servidor, paso A | `cambio_guardia.jsp:150` |
| V4 | Debe haber guardias almacenadas en destino | Servidor, paso A | `cambio_guardia.jsp:167` |
| V5 | No-admin sin vacaciones en destino | Servidor, paso A | `cambio_guardia.jsp:172` |
| V6 | Origen posterior a «ahora»; destino puede ser pasado | Servidor, paso A | `cambio_guardia.jsp:136-139,177` |
| V7 | Sin solicitud PENDIENTE previa del solicitante para el mismo destino | Servidor, paso A | `cambio_guardia.jsp:199-204` |
| V8 | Destinatario activo, mismo tipo, distinto del solicitante | **Solo UI** | `_list_medicos…jsp:150`, `:176-196` |
| V9 | CAMBIO: solicitante sin guardia en destino; destinatario sin guardia en origen | Servidor, paso B | `add_cambio_guardia.jsp:92-104` |
| V10 | CAMBIO: ninguno de vacaciones en el día que recibe | Servidor, paso B | `add_cambio_guardia.jsp:106-133` |
| V11 | CAMBIO con destino PRESENCIA: solicitante sin PRESENCIA el día anterior/posterior al destino (salvo que el anterior sea el origen); exento residente simulado solicitante | Servidor, paso B | `add_cambio_guardia.jsp:136-188` |
| V12 | CESION: destinatario sin vacaciones en origen | Servidor, paso B | `add_cambio_guardia.jsp:207-219` |
| V13 | Siempre, con origen PRESENCIA: destinatario sin PRESENCIA el día anterior/posterior al origen (salvo que el posterior sea el destino); exento si el **solicitante** es residente simulado | Servidor, paso B | `add_cambio_guardia.jsp:226-284` |
| V14 | Debe existir guardia del solicitante en origen y del destinatario en destino | Servidor, paso B | `add_cambio_guardia.jsp:62-74,292` |
| V15 | Admin: autoaprobación | Servidor + cliente | `add_cambio_guardia.jsp:58-59`, `guardias.js:518-526` |
| V16 | Aprobar/cancelar solo admin | **Solo UI** | `lista_cambiosguardias.jsp:94` |
| V17 | Al aprobar, ajuste LOCALIZADA↔REFUERZO del adjunto según presencia de R3/R4/R5 | Servidor | `cambio_estado_guardia.jsp:144-160` y siguientes |

## 5. Preguntas cerradas con evidencia

### Q7 — ¿Qué ocurre en `guardias_medicos` al aprobar?
Ver paso C, punto 11. CAMBIO intercambia médicos (tipo y festivo se quedan con el día); CESION borra y vuelve a crear la guardia para el destinatario; se ajustan las guardias de adjunto si cambia la presencia de R3–R5. CANCELADA/RECHAZADA solo actualiza el estado y envía email.

### Q1 — `VALIDAR_CAMBIOS_GUARDIAS_BY_ADM`
Se edita y persiste (`config/config_form.jsp:165-167`, `:225-226`), pero **no se aplica**: solo se lee en `cambio_guardia.jsp:74` sin usarse. Comportamiento real: las solicitudes de no-admin quedan siempre PENDIENTE y solo un admin puede aprobarlas desde la UI; las del admin se autoaprueban. Funcionalidad inacabada.

### Q6 — Vacaciones globales o por servicio
El alta es `INSERT INTO vacaciones_medicos (IdMedico, FDay)` (`VacacionesDBImpl.java:31`) y las lecturas filtran solo por `IdMedico`/`FDay` (`:198-206`). **Confirmado con el esquema (§8):** `vacaciones_medicos` solo tiene `IdMedico` y `FDay`, con clave única `(IdMedico, FDay)`. Las vacaciones son por médico y globales a todos sus servicios. Que eso sea lo correcto queda para negocio (P9).

### Historial git
- `6599ed2` (2017-07-03) introdujo las validaciones de presencias consecutivas. El bloque del destinatario reutiliza `oSolicitante.isResidenteSimulado()`, lo que sugiere un error de copia (no concluyente).
- `lGuardiasDestinatarioEnOrigen` en CESION se calcula y no se evalúa desde el commit inicial `3cb3f4d`.
- `c0043fd` (2017-10-15) añadió la condición «solo si la guardia es de PRESENCIA».

## 6. Riesgos

### Seguridad e integridad
- **Autorización ausente en servidor:** `add_cambio_guardia.jsp` acepta `IdMedicoSolicitante` y `Estado` del cliente sin contrastarlos con la sesión (`:47`); `cambio_estado_guardia.jsp` no comprueba el rol (`bIsAdministrator` declarado en `:56` y no usado). Cualquier usuario autenticado puede crear cambios en nombre de otro o aprobarlos.
- **Validaciones del paso A no repetidas en B** (V2, V3, V5, V6, V7): se saltan llamando directamente a `add_cambio_guardia.jsp`.
- **Inyección SQL:** fechas del cliente concatenadas (`GuardiasDBImpl.java:575-577`, `VacacionesDBImpl.java:204`).
- **ID `max+1`** (`CambiosGuardiasDBImpl.java:98`): condición de carrera.
- **Sin transacción al aprobar:** varios UPDATE/DELETE/INSERT independientes; un fallo parcial deja el calendario incoherente.
- **Sin revalidación al aprobar:** si cambian vacaciones o guardias entre solicitud y aprobación, se aplica igual.

### Defectos probables
- V7 usa siempre `IdServicio=1` (no se asigna en el paso A; valor por defecto en `CambiosGuardias.java:23`).
- NPE: `GuardiaDestino.getTipo()` (`add_cambio_guardia.jsp:144`) y `GuardiaOrigen.getTipo()` (`:243`, `:269`) antes del null-check (`:292`).
- NPE en aprobación si Google Calendar está desactivado y la guardia tiene `IdEventoGCalendar` (`cambio_estado_guardia.jsp:141`, `:237`).
- Aprobación silenciosa sin efecto: `cambio_estado_guardia.jsp` guarda `APROBADA` (`:83-94`) **antes** de comprobar que el destinatario sigue teniendo guardia en `FechaFinCambio` (`:124-127`). Si no la tiene, no modifica `guardias_medicos` y aun así envía el correo de confirmación (`:394-411`). En el alta no puede ocurrir, porque se rechaza (`add_cambio_guardia.jsp:63`, `:292`); solo ocurre si la guardia del destinatario cambió entre la solicitud y la aprobación. Afecta a CAMBIO y a CESION.
- Un 500 (NPE o fallo de `MailingUtil` tras insert ya confirmado) se muestra como éxito al no-admin (`guardias.js:512-530`).
- Solo se informa el último error (`sError` se sobrescribe).
- Sin probar: `getCambiosGuardiasBySolicitante` recibe un único `Integer` (`lista_cambiosguardias.jsp:52`) pero usa `#{IdMedicoSolicitante}` y `#{IdServicio}` (`CambiosGuardias.xml:73-74`); MyBatis vincularía ambos al mismo valor (`IdServicio = IdMedico`).

### Validaciones dudosas
- CESION: el receptor solo puede elegirse entre quienes tienen guardia en `FechaFinCambio` (`_list_medicos_cambio_guardia.jsp:72`, `:137-158`), y el servidor lo exige (`add_cambio_guardia.jsp:63`, `:292`). Sin embargo, al aplicar la cesión esa guardia del receptor no se modifica (`cambio_estado_guardia.jsp:306-348`): la fecha destino solo sirve de filtro. No consta si es intencionado o un efecto de reutilizar la interfaz del CAMBIO (cuestionario P2).
- CESION: `lGuardiasDestinatarioEnOrigen` calculado y no evaluado (`add_cambio_guardia.jsp:204`).
- V13: la exención de residente simulado mira al solicitante, no al destinatario.
- V6: compara con la hora actual; una guardia de hoy no puede cederse.

### Código muerto / duplicado
- `bValidadobyAdmin` sin uso (`cambio_guardia.jsp:74`); `bIsAdministrator` sin uso (`cambio_estado_guardia.jsp:56`).
- Validaciones comentadas (`cambio_guardia.jsp:155-166`).
- `getCambioGuardiasByMedicoAprobacion` defectuoso (`selectOne` sobre `List`, statement incorrecto) (`CambiosGuardiasDBImpl.java:249`).
- Mapper duplicado en `src/` y `web/WEB-INF/classes/`.

## 7. Preguntas abiertas
Las decisiones de negocio pendientes están en [cambio-guardia-cuestionario-negocio.md](cambio-guardia-cuestionario-negocio.md). Pendiente técnico: comprobar en ejecución el comportamiento de `getCambiosGuardiasBySolicitante`.

## 8. Evidencia de datos

- **Fuente:** dump MySQL `bbdd/Guardias_prod_2026-08-26.db` (MySQL 5.7.33, base `medoncalls_prod`). Su uso para análisis lo autorizó el propietario el 2026-10-24. El fichero está excluido de git (`.gitignore`: `/bbdd/`).
- **Método:** análisis en solo lectura del texto del dump con un script local. Solo se extrajeron agregados de IDs, fechas, estados y tipos. Este documento no incluye nombres, emails, contraseñas ni valores de `configuracion`.
- **Limitación:** es una foto del 2026-08-26. Los cambios posteriores sobre una misma guardia pueden alterar el contraste entre `guardias_cambios` y `guardias_medicos`.

### 8.1 Esquema relevante

| Tabla | Claves e índices | Observaciones |
|---|---|---|
| `guardias_cambios` | **Ninguna** (sin PK ni índice único) | Fechas en `varchar(50)`; `Estado`/`TipoCambio` en `varchar(1000)`; `IdServicio` admite NULL |
| `guardias_medicos` | PK `Id` AUTO_INCREMENT | **Sin unicidad** `(IdMedico, FGuardia, IdServicio)`; `FGuardia` en `varchar(1000)` |
| `vacaciones_medicos` | UNIQUE `(IdMedico, FDay)` | **Sin `IdServicio`** |
| `configuracion` | UNIQUE `(config_key, config_IdServicio)` | Guarda secretos en claro (contraseña de correo, cuenta de servicio de Google) |
| `medicos` | UNIQUE `ID`, UNIQUE `Email` | Columna `Password` en la misma tabla |
| `guardias_servicios_medicos` | PK `(IdServicio, IdMedico)` | Rol de administrador por servicio |

No hay claves foráneas, triggers ni procedimientos almacenados.

### 8.2 Hallazgos

| # | Hallazgo | Dato | Impacto |
|---|---|---|---|
| D1 | Todos los cambios están APROBADA | 5.484 de 5.484; 0 PENDIENTE, CANCELADA o RECHAZADA | El circuito solicitud → aprobación **no se usa en la práctica** |
| D2 | Los registran los administradores en nombre de los médicos | Aprobación el mismo día que la creación: 5.475/5.484. Aprobador administrador: 100 % (2 administradores). Solicitante sin rol de administrador: 5.190 | El flujo real es «el administrador registra y autoaprueba» (V15) |
| D3 | El riesgo de `max+1` se ha materializado | 3 `IdCambio` duplicados | Confirma la condición de carrera; la tabla no tiene PK |
| D4 | Cambios repetidos | 44 combinaciones (solicitante, origen, destino) duplicadas | Doble envío o falta de idempotencia |
| D5 | APROBADA sin aprobador | 8 cambios con `UsuarioAprobacion` NULL | Probable autoaprobación cuya llamada a `cambio_estado_guardia.jsp` falló: posible cambio **no aplicado** |
| D6 | Las cesiones siempre tienen dos fechas | 3.582 cesiones, todas con `FechaIni ≠ FechaFin` | Coherente con P2: la cesión se modela como un arrastre entre dos días |
| D7 | Mezcla de tipos de médico | 627 cambios (11 %) entre médicos de distinto tipo | V8 (mismo tipo) no se cumple en los cambios que registran los administradores |
| D8 | Cesión a quien ya tenía guardia | 6 cesiones con el destinatario con más de una guardia el día origen | Ocurre, pero es raro (P1) |
| D9 | Varias guardias del mismo médico el mismo día | 3 casos (localizada+presencia: 2; presencia+refuerzo: 1) | Sin restricción de unicidad en la base de datos |
| D10 | Presencias consecutivas | 3 pares en todo el calendario | La regla se cumple casi siempre (P4) |
| D11 | Fechas | 4 cambios con origen pasado, 1 del mismo día y 18 con destino pasado | Excepciones raras, posiblemente registros de administradores (P3) |
| D12 | Multiservicio | Servicio 1: 10.750 guardias y todos los cambios. Servicio 2: 62 guardias y ningún cambio. Solo 1 médico en más de un servicio, sin vacaciones | La multitenencia apenas se usa; P9 tiene poco impacto hoy |
| D13 | Calidad de datos en `medicos` | Hay ADJUNTOS con `SubTipo` R1 (23) y R5 (2) | `SubTipo` parece un valor por defecto sin significado para adjuntos: hay que confirmarlo antes de migrar |
| D14 | Lista de cambios sin rol de administrador | 286 cambios con `IdServicio = IdMedicoSolicitante` | Coherente con el riesgo de vinculación de parámetros de MyBatis; sigue sin probar en ejecución |

### 8.3 Consecuencias para la especificación
- El caso de uso dominante es **«el administrador registra un cambio ya acordado»**, no «el médico solicita y el administrador aprueba». La spec debería priorizarlo y decidir si el flujo de solicitud se mantiene (P6).
- Las reglas V8 y V9–V13 se aplican de forma distinta según quién registre el cambio. Hay que decidir si los administradores pueden saltárselas.
- La migración de datos tiene que tratar los `IdCambio` duplicados (D3), los cambios repetidos (D4), los 8 cambios sin aprobador (D5) y los `SubTipo` de los adjuntos (D13).
