# Validación: funcionalidad 001

Registro de las comprobaciones de la fase de cierre (tasks.md, Phase 7). Solo se anota como superado lo que se ha ejecutado (principio V).

## T064: creación de los triggers en MySQL 5.7 con binlog (R11)

**Fecha**: 2026-10-25.

**Entorno**:
- Contenedor `mysql:5.7` (5.7.44) con `--log-bin=mysql-bin --server-id=1`, esquema legacy (`legacy-schema.sql`) y sin datos.
- Usuario `migra` con `ALL PRIVILEGES ON guardias.*` y sin `SUPER`.
- Migraciones lanzadas por la imagen del backend (`guardias-backend:local`) con Liquibase activado.

| Paso | Resultado |
|---|---|
| Migrar con `log_bin = 1`, `log_bin_trust_function_creators = 0` | **Falla** el changeset `003-medicos-tipo-historico-trigger-update` con el error 1419: *You do not have the SUPER privilege and binary logging is enabled*. Liquibase se detiene con 001, 002 y las dos primeras partes de 003 ya aplicadas, porque en MySQL el DDL no es transaccional. |
| `SET GLOBAL log_bin_trust_function_creators = 1` (root) y relanzar | Liquibase continúa desde el changeset que falló y termina: 001–005 ejecutados, triggers `trg_medicos_tipo_insert` y `trg_medicos_tipo_update` creados. |
| Alta de un R2 y cambio a R3 | El histórico queda con R2 (hasta hoy) y R3 (desde hoy). |

**Conclusiones para el runbook (T065)**:
- Antes de migrar hay que consultar en producción `@@log_bin` y `@@log_bin_trust_function_creators`.
- Si el binlog está activo, hay que hacer una de dos cosas:
  - lanzar las migraciones con un usuario con `SUPER`;
  - o activar `log_bin_trust_function_creators = 1` durante la migración.
- Si una migración se interrumpe, se corrige la causa y se relanza: Liquibase retoma desde el changeset que falló.
- **Pendiente**: comprobar con el responsable el binlog y los privilegios reales del usuario de migración en producción. Aquí se ha reproducido la configuración más restrictiva.

## T063: generador de agenda del legacy con `UX_GUARDIA_MEDICO_DIA` (R8)

**Estado**: **análisis de código hecho; la ejecución está pendiente.**

Ejecutarlo requiere arrancar el legacy contra una MySQL 5.7 de prueba. La conexión del legacy está en `base.properties` y `ConfigurationIBATIS.xml`: contienen credenciales y no se leen ni se modifican en esta funcionalidad. Hace falta que el responsable apunte una instancia del legacy a la base de `deploy/local` (o a otra de prueba).

**Lo que dice el código**:
- La agenda se calcula en el navegador (`web/calc_agenda.jsp`, `web/fill_agenda.jsp`, `web/js/guardias.js`) y se envía como JSON a `web/medico/grabar_guardias.jsp`.
- `grabar_guardias.jsp` borra, para cada día de la agenda, todas las guardias del servicio (`GuardiasDBImpl.DeleteGuardia(-1, día, servicio)`). Después inserta cada guardia con `GuardiasDBImpl.AddGuardia`, en autocommit y una a una.
- `AddGuardia` (`src/com/guardias/database/GuardiasDBImpl.java:180-228`) captura `SQLException`, escribe la traza y devuelve `true`.
- Por tanto, con el índice único, si la agenda enviada asigna al mismo médico dos veces el mismo día y servicio:
  - la segunda inserción **falla en silencio**: no hay error para el usuario y se pierde esa guardia duplicada;
  - el resto de la agenda se graba;
  - antes del índice, el duplicado se guardaba.
- Si la agenda no contiene duplicados, el comportamiento no cambia: el borrado por día deja libre el índice antes de insertar.

**Prueba a ejecutar** (cuando haya un legacy apuntando a una base de prueba con las migraciones aplicadas):
1. Generar y grabar la agenda de un mes con datos ficticios. Esperado: misma agenda que sin el índice y ninguna traza `Duplicate entry … UX_GUARDIA_MEDICO_DIA` en el log de Tomcat.
2. Repetir la grabación del mismo mes, que borra y vuelve a insertar. Esperado: igual que el punto 1.
3. Forzar desde la interfaz de la agenda el mismo médico dos veces en un día y grabar. Esperado según el código: se graba una sola, con traza de error en el log. Confirmar con negocio si es aceptable o si el legacy debe avisar.

## T067: pruebas y escenario manual

Pendiente (fuera del alcance T059–T066).
