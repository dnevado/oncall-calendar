# Descubrimiento legacy: vacaciones de los médicos

- **Fecha:** 2026-10-25
- **Alcance:** alta, consulta y baja de días de vacaciones (selector de varias fechas en la ficha del médico), tabla `vacaciones_medicos`, uso de las vacaciones en otras pantallas (generación de agenda, cambios y cesiones, listado de médicos, configuración) y quién puede gestionarlas. Es la base de la especificación 002.
- **Método:** skill `legacy-discovery`. Se leyó el código sin modificarlo. Además se consultaron **solo agregados** de la copia local autorizada de la base (`guardias-local-mysql-1`), sin datos personales. No se ejecutó el legacy.
- **Estado:** borrador pendiente de validación por negocio. Lo marcado como *inferido* se dedujo del código y no se ha reproducido.

## 1. Mapa del flujo

### Paso A: abrir la ficha del médico
1. Hay tres puntos de entrada a `EditarMedico(ID)`, que carga `medico/detallemedico.jsp?id=ID` en un diálogo jQuery UI (`web/js/guardias.js:49-58`):
   - **«Tu perfil»** y el nombre del usuario en la cabecera, para **cualquier médico** sobre su propia ficha (`web/common/navigation.jsp:85`, `:91`);
   - el listado de médicos del servicio: fila o botón «Nuevo» (`web/medico/lista_medicos.jsp:63`, `:135`);
   - la búsqueda de médicos (`web/medico/results_doctor.jsp:59`, `:99`) y «Agregar miembro» (`web/servicio/lista_servicios.jsp:86`).
2. La carga de la ficha (rama GET) lee las vacaciones del médico desde el día 1 del mes anterior hasta hoy + 2 años (`web/medico/detallemedico.jsp:291-298`). Las vuelca en el array JS `holidays[]` (`:590-595`).
3. Restricción de interfaz para quien no es administrador: campos `readonly` y botones `disabled`, entre ellos el botón «Vacaciones» (`:29-35`, `:599`). Es **solo cliente**.

### Paso B: elegir fechas
4. Al pulsar «Vacaciones» se ejecuta `date_()` (`:370-385`). Inicializa jQuery UI MultiDatesPicker 1.6.3 sobre `<div id="vacaciones">` (`:583`):
   - opciones `dateFormat "yy-mm-dd"`, `minDate "-1M"`, `maxDate "+12M"`, `firstDay 1` y nombres de días y meses en español;
   - después ejecuta `addDates(holidays)` para marcar las vacaciones ya guardadas.
5. **El selector solo existe tras pulsar el botón.** Antes de eso, el plugin crea `dates.picked` vacío en un elemento sin inicializar (`web/js/jquery-ui.multidatespicker.js:418-470`).

### Paso C: guardar la ficha, y con ella las vacaciones
6. El submit del formulario (`:481`, botón «Guardar» `:610`, activo **también para no administradores**) llama a `_GuardarMedico()` (`:413-470`):
   - construye `obj` con todos los campos de la ficha;
   - incluye `obj['vacaciones'] = $('#vacaciones').multiDatesPicker('getDates')` (`:437`, que pisa el `:428`);
   - hace un POST AJAX síncrono a la misma JSP. jQuery serializa el array como `vacaciones[]`.
7. Rama de guardado (`request.getParameter("guardar")`, `:49`):
   - `aVacaciones = request.getParameterValues("vacaciones[]")` (`:104`);
   - **validación (V1):** para cada día comprueba si el médico tiene guardia ese día **en el servicio de la sesión** (`GuardiasDBImpl.getGuardiasMedicoFecha(ID, dia, servicioSesión)`, `:143-165`). Si la tiene, responde `NOOK Existe el día de vacaciones <fecha> con guardia activa`, corta en el primer conflicto y no guarda nada;
   - si no hay error, da de alta o actualiza el médico (`:166-203`) y después reemplaza las vacaciones (`:205-248`):
     - calcula `desde = min(día 1 del mes actual, día seleccionado más antiguo)` comparando cadenas `yyyy-MM-dd` (`:211-231`);
     - `VacacionesDBImpl.DeleteVacacionesMedicoDesde(ID, desde)`, que borra **todos** los días del médico `>= desde` sin límite superior (`:235`, SQL en `VacacionesDBImpl.java:127`);
     - `AddVacacionesMedico(ID, dia)` para cada día recibido (`:237-246`).
   - El cliente muestra «éxito» o el mensaje `NOOK` (`:450-466`).

### Paso D: consumidores de las vacaciones

| Consumidor | Uso | Evidencia |
|---|---|---|
| Generación de agenda (`calc_agenda.jsp`) | Excluye a quien está de vacaciones ese día al asignar presencias, refuerzos y localizadas | `web/calc_agenda.jsp:525`, `:581`, `:588`, `:594`, `:856`, `:924`; `ProcesarMedicos.NoTieneVacaciones` (`src/com/guardias/ProcesarMedicos.java:2505-2518`) y usos en `:148`, `:304`, `:423`, `:1944`, `:2039`, `:2226`, `:2349` |
| Generación de agenda | Descuenta los días de vacaciones de los adjuntos de la capacidad del mes: `adjuntos × 30 − Σ vacaciones` | `web/calc_agenda.jsp:206-249`; `ProcesarMedicos.getNumeroVacacionesMedico` (`:1031-1039`) |
| Generación de agenda, **meses vacacionales** | En los meses configurados (`AJUSTE_A_ESTOS_MESES_VACACIONES`) no se asigna al azar: residentes y adjuntos se ordenan por número de días de vacaciones del mes, de más a menos (orden aleatorio entre iguales, simulados al final). Se usa un reparto específico de localizadas y refuerzos | `web/calc_agenda.jsp:85`, `:145`, `:707`, `:756`, `:1120-1126`; `ProcesarMedicos.java:2438-2500`, `:114-204`; `ByVacacionesDESCResidentes.java:11`; `Util.EsMesVacaciones` (`Util.java:477-488`) |
| Equilibrio histórico en meses vacacionales | El total histórico de guardias de un médico solo cuenta los meses vacacionales | `src/com/guardias/database/GuardiasDBImpl.java:377-395`; `Util.ListaMesesVacaciones` (`Util.java:456-473`) |
| Relleno manual de agenda (`fill_agenda.jsp`) | No ofrece a quien está de vacaciones ese día | `web/fill_agenda.jsp:128` |
| Cambio o cesión, prevalidación al arrastrar | Para **no administradores**: error si el médico está de vacaciones en la fecha destino | `web/medico/cambio_guardia.jsp:134`, `:172-176`; mensaje `cambio_guardias.existe_vacaciones` |
| Cambio o cesión, alta | Error si el solicitante está de vacaciones en la fecha fin o el destinatario en la fecha inicio (cambio), o si el receptor está de vacaciones en la fecha origen (cesión). Es la regla **CG-012, ya migrada** | `web/medico/add_cambio_guardia.jsp:105-131`, `:206-217`; mensaje `cambio_guardias.existe_vacaciones_medico_fecha`; nuevo backend `ReglasCambio.java:98-99`, `VacacionesMapper.xml` |
| Listado de médicos (admin) | Columna «Vacaciones»: casillas deshabilitadas con los **meses** con vacaciones del mes siguiente a +6 meses | `web/medico/lista_medicos.jsp:78`, `:98-110`, `:124`, `:146-157`; `SQLGENERAL_MESES` (`VacacionesDBImpl.java:17`) |
| Configuración del servicio | Selector múltiple de meses vacacionales, guardado como `"7\|8\|9"` | `web/config/config_form.jsp:193-195`, `:242-243`, `:443-460`; alta de servicio `web/servicio/servicio_form.jsp:133` |

El calendario legacy (`web/inicio.jsp`, `web/js/guardias.js`) **no muestra** las vacaciones: no hay ninguna referencia a ellas.

## 2. Artefactos

| Tipo | Ruta | Responsabilidad | Dependencias |
|---|---|---|---|
| JSP (vista y acción) | `web/medico/detallemedico.jsp` | Ficha del médico: carga, selector de vacaciones, validación y guardado | `VacacionesDBImpl`, `GuardiasDBImpl`, `MedicoDBImpl`, MultiDatesPicker |
| JS | `web/js/jquery-ui.multidatespicker.js` (1.6.3) | Selección de varias fechas sobre jQuery UI datepicker | jQuery UI |
| JS | `web/js/guardias.js:49` | `EditarMedico`: abre la ficha en un diálogo | jQuery UI dialog |
| DAO | `src/com/guardias/database/VacacionesDBImpl.java` | Alta, bajas y consultas (detalle, rango, meses y recuento) | `ConexionGuardias` (JDBC directo, **no** iBATIS) |
| Entidad | `src/com/guardias/Vacaciones_Medicos.java` | `IDMEDICO` y `DiaVacaciones`. En las consultas `SQLCOUNT` y `SQLGENERAL_MESES` estos campos guardan el recuento y el mes | — |
| Lógica | `src/com/guardias/ProcesarMedicos.java` | `NoTieneVacaciones`, `getNumeroVacacionesMedico`, orden por vacaciones y reparto en meses vacacionales | `VacacionesDBImpl` |
| Lógica | `src/com/guardias/Util.java:174-218`, `:456-488` | Clave de configuración y meses vacacionales | `ConfigurationDBImpl` |
| Comparador | `src/com/guardias/ByVacacionesDESCResidentes.java` | Orden descendente por días de vacaciones del mes | `Medico.VacacionesMes` (`Medico.java:48-55`) |
| Tabla | `vacaciones_medicos` | `IdMedico int NOT NULL`, `FDay varchar(10) NOT NULL`, `UNIQUE (IdMedico, FDay)`. **No tiene `IdServicio`** ni clave ajena | — |
| Tabla | `configuracion` (`config_key`, `config_value`, `config_IdServicio`) | `AJUSTE_A_ESTOS_MESES_VACACIONES` por servicio | — |
| Nuevo backend | `backend/.../cambios/infrastructure/VacacionesMapper.*` | Lectura por lotes para CG-012 (solo lectura) | MyBatis |

## 3. Widgets e interacción

| Widget legacy | Configuración | Capacidad para el usuario |
|---|---|---|
| jQuery UI MultiDatesPicker 1.6.3 en línea (`detallemedico.jsp:372-383`) | Formato `yy-mm-dd`; rango de −1 mes a +12 meses; semana desde el lunes; textos en español; precarga de los días guardados | **Elegir y quitar varias fechas sueltas a la vez** sobre un calendario mensual (capacidad que se conserva, CLAUDE.md). No permite elegir rangos de una vez: cada día es un clic |
| Botón «Vacaciones» | Inicializa el selector bajo demanda; deshabilitado para quien no es administrador | Mostrar u ocultar el calendario de vacaciones |
| Diálogo jQuery UI «Datos del médico» (`guardias.js:49-58`, ancho 30 %) | Formulario único con datos del médico y vacaciones | Editar ficha y vacaciones con un solo «Guardar» |
| Mensajes `#error` / `#success` (`detallemedico.jsp:450-466`, `:485-495`) | Texto del servidor sin el prefijo `NOOK` | Ver el conflicto con una guardia o la confirmación |
| Casillas deshabilitadas en `lista_medicos.jsp:146-157` | Una por mes con vacaciones (próximos 6 meses) | Ver de un vistazo qué meses tiene cada médico con vacaciones |
| `<select multiple>` de meses en `config_form.jsp:446-460` | De enero a diciembre | Elegir los meses vacacionales del servicio |

## 4. SQL y tablas

| Operación | SQL (literal o resumido) | Evidencia | L/E | Observación |
|---|---|---|---|---|
| Alta de día | `INSERT INTO vacaciones_medicos (IdMedico, FDay) VALUES (?, ?)` | `VacacionesDBImpl.java:31-47` | E | Autocommit; la `SQLException` se captura y se ignora (un duplicado no avisa) |
| Baja desde fecha | `DELETE FROM vacaciones_medicos WHERE IdMedico = ? AND FDay >= ?` | `:127` | E | Sin límite superior; fuera de transacción con las altas |
| Baja de un día | `DELETE vacaciones_medicos WHERE IdMedico (?) and Day=(?)` | `:70-82` | E | **SQL inválido** (falta `FROM`, falta `=`, la columna no se llama `Day`). Nadie la llama: código muerto |
| Consulta de un día o de un rango | `SELECT * FROM vacaciones_medicos WHERE IdMedico=<id> [and FDay='<d>' \| and FDay>='<a>' and FDay<='<b>']` | `:15`, `:183-207` | L | **Concatenación de cadenas** (`Statement`); fechas de origen interno |
| Meses con vacaciones | `SELECT DISTINCT idmedico, EXTRACT(MONTH FROM date(fday)) fday FROM vacaciones_medicos ...` | `:17` | L | Devuelve el número de mes en el campo del día |
| Recuento | `SELECT count(*) idmedico FROM vacaciones_medicos ...` | `:19`, `:158-162` | L | Devuelve el recuento en el campo `IDMEDICO` |
| Conflicto con guardia | `GuardiasDBImpl.getGuardiasMedicoFecha(id, dia, servicio)` | `detallemedico.jsp:150` | L `guardias_medicos` | Solo mira el servicio de la sesión |
| Meses vacacionales | `ConfigurationDBImpl.GetConfiguration("AJUSTE_A_ESTOS_MESES_VACACIONES", servicio)` / `UpdateConfiguracion` | `Util.java:460`, `config_form.jsp:193-195` | L/E `configuracion` | — |
| Lectura nueva (CG-012) | `SELECT IdMedico, FDay FROM vacaciones_medicos WHERE IdMedico IN (...) AND FDay IN (...)` | `backend/src/main/resources/mappers/VacacionesMapper.xml` | L | Parametrizada |

### Datos (copia local autorizada, solo agregados)
- La tabla tiene 4 105 filas de 16 médicos, entre 2017-06-30 y 2026-09-30. No hay ninguna a partir de 2026-10-01.
- Todas las fechas tienen formato `yyyy-MM-dd` (0 filas con otro formato).
- No hay filas huérfanas: todas apuntan a un médico existente.
- **5 días de vacaciones coinciden con una guardia del mismo médico** (cualquier servicio). La validación V1 solo se aplica al guardar la ficha, no al generar ni cambiar guardias después. Tampoco cubre otros servicios.
- `AJUSTE_A_ESTOS_MESES_VACACIONES = 7|8|9` en los servicios 1 a 4.

## 5. Validaciones y decisiones

| Id | Decisión | Capa | Evidencia |
|---|---|---|---|
| V1 | No se puede marcar de vacaciones un día en el que el médico tiene guardia (en el servicio de la sesión) | Servidor | `detallemedico.jsp:143-165` |
| V2 | Solo se pueden elegir días entre −1 mes y +12 meses desde hoy | **Solo cliente** | `detallemedico.jsp:372` |
| V3 | Solo el administrador puede editar vacaciones | **Solo cliente** (botón deshabilitado) | `detallemedico.jsp:29-35`, `:599`; la rama de guardado (`:49-250`) no comprueba el rol |
| V4 | Guardar sustituye todas las vacaciones desde `min(día 1 del mes actual, día elegido más antiguo)` | Servidor | `detallemedico.jsp:205-248` |
| V5 | Un mismo día no se repite por médico | SQL (`UNIQUE IND_VACACIONES_01`) | Esquema |
| V6 | Las vacaciones son **del médico**, no del servicio: bloquean en todos sus servicios | Datos (sin `IdServicio`) | Esquema; CG-012 «en cualquier servicio» (`ReglasCambio.java:98`) |
| V7 | Quien está de vacaciones no recibe guardias en la agenda automática ni en la manual | Servidor | §1 Paso D |
| V8 | En meses vacacionales, prioridad de asignación por número de días de vacaciones del mes (de más a menos) | Servidor | §1 Paso D |
| V9 | No se puede ceder o cambiar una guardia a un día de vacaciones del receptor o del solicitante | Servidor (migrada como CG-012) | `add_cambio_guardia.jsp:105-131`, `:206-217` |

## 6. Riesgos y hallazgos

| # | Severidad | Hallazgo | Evidencia | Confianza |
|---|---|---|---|---|
| R1 | **Alta** | **Pérdida silenciosa de vacaciones.** Si se guarda la ficha sin pulsar antes «Vacaciones», el cliente envía la lista vacía y el servidor borra todas las vacaciones del médico desde el día 1 del mes actual. Afecta a cualquier médico que edite «Tu perfil» (no puede abrir el selector) y al administrador que edite otro dato | `detallemedico.jsp:437`, `:211-235`; `multidatespicker.js:418-470` | Alta (inferido, **no reproducido**) |
| R2 | **Alta** | **Sin control de permisos en el servidor.** Cualquier usuario autenticado puede enviar `vacaciones[]` (y el resto de la ficha) para cualquier `ID` | `detallemedico.jsp:49-250` | Alta |
| R3 | Media | Borrado y altas sin transacción, y errores de alta ignorados: un fallo a mitad deja las vacaciones a medias sin avisar | `VacacionesDBImpl.java:31-60`, `:127-150` | Alta |
| R4 | Media | V1 solo mira el servicio de la sesión; un médico en varios servicios puede tener vacaciones el día de una guardia de otro servicio. Además nada impide asignar guardias después (5 casos en datos) | `detallemedico.jsp:150`; §4 datos | Alta |
| R5 | Media | Días fuera de la ventana del selector: se cargan desde el día 1 del mes anterior hasta +2 años, pero el selector solo deja de −1 mes a +12 meses. No se sabe si `addDates` conserva los días fuera de rango; si no, al guardar se borrarían las vacaciones a más de 12 meses | `detallemedico.jsp:291-298`, `:372-383` | Media (pendiente de prueba) |
| R6 | Media | SQL por concatenación (`getVacacionesMedicosFiltro`). Hoy las fechas vienen del servidor, pero el patrón es inseguro | `VacacionesDBImpl.java:198-207` | Alta |
| R7 | Baja | `DeleteVacacionesMedico` tiene SQL inválido y nadie la usa (código muerto) | `VacacionesDBImpl.java:70-82` | Alta |
| R8 | Baja | `config_form.jsp` divide con `split("\|")` sin escapar: la regex parte carácter a carácter, así que los meses 10, 11 y 12 no aparecen marcados (y marca el 1). Al guardar se perderían si no se vuelven a elegir. Con `7\|8\|9` no se nota | `config_form.jsp:443` frente a `Util.java:463` | Alta |
| R9 | Baja | `FDay` es `varchar(10)`: las comparaciones de rango funcionan por orden lexicográfico de `yyyy-MM-dd`, sin tipo fecha | Esquema | Alta |
| R10 | Baja | Las vacaciones no se ven en el calendario de guardias; solo en la ficha y, por meses, en el listado del administrador | §1 Paso D | Alta |

## 7. Preguntas abiertas para negocio

1. **¿Quién gestiona las vacaciones?** ¿Solo el administrador del servicio (lo que sugiere el botón deshabilitado) o también cada médico las suyas? ¿Las de un médico con varios servicios las gestiona cualquiera de sus administradores?
2. **¿Hace falta aprobación**, o el alta es directa como hoy?
3. **Ventana de fechas:** ¿se mantiene «desde 1 mes atrás hasta 12 meses adelante»? ¿Se pueden corregir vacaciones ya pasadas?
4. **Conflicto con guardia:** ¿se mantiene el rechazo (V1)? ¿Debe mirar guardias de todos los servicios del médico? ¿Qué hacer con los 5 casos existentes?
5. **¿Debe impedirse asignar o mover una guardia a un día de vacaciones** en todas las vías (agenda, cambios del administrador)? Hoy el administrador, al arrastrar, se salta la prevalidación de `cambio_guardia.jsp:172`, aunque el alta (`add_cambio_guardia.jsp`) sí lo comprueba.
6. **¿Elegir rangos** (del día X al día Y) además de días sueltos? El legacy solo permite días sueltos.
7. **¿Mostrar las vacaciones en el calendario de guardias** o en una vista de equipo? Hoy no se muestran.
8. **¿Hay un máximo** de días por médico, por mes o por año, o un mínimo de plantilla disponible por día? En el legacy no existe ninguno.
9. **¿Notificar** el alta o la baja de vacaciones (por correo, como en los cambios)?
10. ¿El comportamiento R1 (borrado al guardar la ficha) ha causado incidencias conocidas? Ayuda a priorizar la corrección mientras conviven legacy y nueva aplicación.
11. Meses vacacionales (V8): ¿quedan fuera de esta funcionalidad (pertenecen a la generación de agenda) o se incluye su configuración?

## 8. Implicaciones para la especificación 002 (para debatir, no son decisiones)

- La gestión de vacaciones puede separarse de la ficha del médico. Así se elimina R1 de raíz en la aplicación nueva, pero R1 **sigue existiendo en el legacy** mientras convivan.
- El permiso debe comprobarse en el servidor (principio II), a diferencia de V3.
- Mientras convivan las dos aplicaciones, la tabla `vacaciones_medicos` es compartida. Cualquier escritura nueva debe respetar el formato `yyyy-MM-dd` y la unicidad, y no requerir cambios de esquema que rompan el legacy.
- CG-012 ya lee la tabla; la nueva escritura no debe cambiar su semántica.
