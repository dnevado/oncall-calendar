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

## Ensayo de migración sobre una copia de producción (runbook, pasos 2–4)

**Fecha**: 2026-10-25.

**Decisión del responsable**: cargar el volcado de producción `bbdd/Guardias_prod_2026-08-26.db` en la base local de `deploy/local`, sustituyendo los datos ficticios. Es una excepción al principio VI, pedida expresamente por el responsable. La copia vive solo en el volumen Docker local: no va a git, y aquí no se anotan datos personales.

**Entorno**: contenedor `mysql:5.7` (5.7.44) de `deploy/local`, sin binlog. El volcado viene de MySQL 5.7.33 y se carga en la base `guardias`, quitando sus líneas `CREATE DATABASE` y `USE`.

| Paso | Resultado |
|---|---|
| Carga | 8 tablas: 10.812 guardias, 5.484 cambios, 39 médicos y 4 servicios |
| Arranque del backend (Liquibase) | 001–003 aplicados. **004 se detiene en su precondición** por los `IdCambio` repetidos, como prevé el runbook |
| Diagnóstico (`limpieza-duplicados.sql`, consultas 1 y 2) | 3 `IdCambio` repetidos (1535, 1536, 2754). Cada par son **copias exactas** de una cesión que ya estaba en otro `IdCambio`: la del 1534 aparece 5 veces y la del 2753, 3 veces. Además, hay 3 médicos con dos guardias el mismo día y servicio (2022-01-25, 2026-08-17 y 2026-08-25): siempre una de presencia y otra de refuerzo o localizada, y la de presencia tiene el `Id` más alto |
| Decisión del responsable | Renumerar una copia de cada `IdCambio` repetido (no se pierde ninguna fila y el equilibrio del legacy no cambia). Borrar la guardia que no es de presencia en los 3 casos (`Id` 19010, 11404 y 19052) |
| Corrección (una transacción) | 3 filas renumeradas (nuevos `IdCambio` 5485–5487) y 3 guardias borradas. El diagnóstico vuelve a dar 0 en las dos consultas. `COMMIT` |
| Relanzar el backend | Liquibase retoma desde 004: 004 y 005 aplicados y el backend arranca |
| Comprobaciones del paso 4 | 8 changesets. Triggers `trg_medicos_tipo_insert` y `trg_medicos_tipo_update` creados. Índice único `UX_GUARDIA_MEDICO_DIA` creado. `IdCambio` es clave primaria con `AUTO_INCREMENT`. Histórico de tipo con 39 filas, una por médico |

**Para producción**:
- Las decisiones de esta tabla son las que se proponen para el paso 3 del runbook. Allí se vuelven a confirmar con los IDs delante.
- La cesión del 1534 sigue registrada 5 veces (1534, 1535, 1536, 5485 y 5486), y la del 2753, 3 veces (2753, 2754 y 5487). Renumerar mantiene el recuento actual del legacy. Si esas repeticiones deben contar una sola vez, es la pregunta abierta de duplicados (CG-016).
- En la copia, `ACTIVAR_CAMBIO_GUARDIAS` vale `S` en los 4 servicios. El paso 5 lo cambiará en producción.

**Guardias futuras de prueba en la copia** (2026-10-25, a petición del responsable):
- **Por qué**: el volcado solo tiene guardias hasta el 2026-10-31.
- **Qué se ha insertado**: 270 guardias del servicio 1, del 2026-11-01 al 2027-01-30, con `Id` 19466–19735.
- **Cómo se han generado**:
  - Son las del 2026-08-02 al 2026-10-31, desplazadas 91 días. Al ser 13 semanas exactas, cada guardia cae en el mismo día de la semana y se conservan médicos, tipos y dotación.
  - `Festivo` sigue la regla observada en el legacy: sábado, domingo y festivos nacionales (2026-12-08, 2026-12-25, 2027-01-01 y 2027-01-06).
- **Validez**: no son datos reales. Solo sirven para probar la interfaz y las reglas en la copia local.
- **Borrado**: `DELETE FROM guardias_medicos WHERE Id BETWEEN 19466 AND 19735;`, siempre que antes no se haya registrado ningún cambio sobre ellas.

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

**Fecha**: 2026-10-25. **Versión**: commit `93345ce`.

### Pruebas automáticas (ejecutadas)

| Comando | Resultado |
|---|---|
| `mvn -f backend/pom.xml verify` (Testcontainers, `mysql:5.7`) | Correcto (exit 0): 58 pruebas unitarias y de contrato (surefire) y 58 de integración (failsafe), sin fallos, errores ni omitidas |
| `npm --prefix frontend test` | 5 ficheros, 32 pruebas, todas correctas (11 del calendario: vistas, navegación, orden, leyenda, teclado) |
| `npm --prefix frontend run lint` | Sin errores |
| `npm --prefix frontend run build` | Correcto |

### Escenario manual (quickstart §2)

**Estado**: **pendiente**. No se ha podido ejecutar: en esta sesión no hay navegador automatizable (Claude in Chrome no conectado). Entorno preparado según `deploy/local/README.md`: MySQL 5.7 de `deploy/local`, backend en el puerto 8081 y Vite en `http://localhost:5173/app/`.

- Pasos 2–7: los ejecuta el responsable en el navegador, también en ancho de móvil (H4). Se anota aquí el resultado de cada paso.
- **Comprobado por el responsable (2026-10-25)**, sobre la copia local con guardias futuras de prueba:
  - El calendario carga y se ven las guardias de noviembre.
  - El arrastrar y soltar funciona (FR-032).
  - Pendiente: el resto de puntos del paso 2, los pasos 3–7 y la vista en móvil.
- Paso 8 (legacy): requiere un legacy apuntando a la base de prueba, como T063.

Las reglas y efectos de los pasos 3–7 están cubiertos por las pruebas de integración contra MySQL 5.7 (`RegistrarCesionIT`, `RegistrarIntercambioIT`, `AjusteResidentesIT`, `LegacyVisibilidadIT`). Esa cobertura no sustituye a la comprobación de la interfaz.
