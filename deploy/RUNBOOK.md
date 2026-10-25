# Runbook: puesta en marcha de la funcionalidad 001 (registro de cambios y cesiones)

Pasos para poner en producción el sistema nuevo junto al legacy. **Cada paso que modifica datos de producción exige la confirmación explícita del responsable del proyecto en ese momento**; una aprobación anterior no vale para el paso siguiente (constitución; plan 001, «Approvals»).

Anota en el registro de la puesta en marcha (fecha, persona, resultado) cada paso marcado con ☐.

## 0. Antes de la ventana

- ☐ Pruebas en verde con la versión a desplegar: `mvn -f backend/pom.xml verify` y `npm --prefix frontend test` (T067).
- ☐ Imágenes construidas con esa versión: `GUARDIAS_VERSION=<versión> docker compose -f deploy/docker-compose.yml build`.
- ☐ TLS delante del proxy (`deploy/README.md`, «Requisitos»). Sin HTTPS, el navegador no envía las cookies de sesión.
- ☐ Secretos del backend creados en el servidor (`deploy/secrets/README.md`). El usuario de base de datos del backend tiene solo los privilegios necesarios.
- ☐ Fichero de entorno de **migración**, aparte y con permisos `600` (p. ej. `deploy/secrets/migracion.env`, ignorado por git):
  ```
  LIQUIBASE_COMMAND_URL=jdbc:mysql://<host>:3306/<base>?useSSL=false
  LIQUIBASE_COMMAND_USERNAME=<usuario de migración>
  LIQUIBASE_COMMAND_PASSWORD=<contraseña>
  ```
- ☐ Configuración del binlog en producción, consultada con el responsable (`specs/001-registro-cambios-guardia/validacion.md`, T064):
  ```sql
  SELECT @@version, @@log_bin, @@log_bin_trust_function_creators;
  ```
  Si `@@log_bin = 1`, crear los triggers exige una de dos cosas: un usuario de migración con `SUPER`, o `SET GLOBAL log_bin_trust_function_creators = 1` durante la migración (y volver al valor anterior después). Se decide con el responsable.
- ☐ Ventana acordada, con el legacy sin uso durante los pasos 1 a 4.

## 1. Copia de seguridad verificada

1. ☐ Volcado completo:
   ```sh
   mysqldump --single-transaction --routines --triggers --events <base> > guardias_AAAAMMDD_HHMM.sql
   ```
2. ☐ **Restaurarlo en un entorno de pruebas** y comprobar los recuentos de `guardias_medicos`, `guardias_cambios`, `medicos` y `configuracion` frente a producción.
3. ☐ Guardar el volcado fuera del servidor, con acceso restringido: contiene datos personales.

Sin este paso verificado no se continúa.

## 2. Diagnóstico de duplicados

1. ☐ Ejecutar **solo las consultas** de `deploy/db/limpieza-duplicados.sql` (secciones 1 y 2).
2. ☐ Revisar con el responsable cada `IdCambio` repetido y cada médico con dos guardias el mismo día y servicio. Qué fila sobra es una decisión de negocio para cada caso.
3. ☐ Anotar los IDs concretos y la decisión tomada en cada uno.

## 3. Corrección (solo con confirmación explícita)

1. ☐ **Confirmación explícita del responsable**, con los IDs del paso 2 delante.
2. ☐ Preparar las sentencias a partir de las plantillas comentadas de `limpieza-duplicados.sql`, con los IDs reales. Revisarlas con el responsable.
3. ☐ Ejecutarlas en una transacción, comprobar las filas afectadas y confirmar (`COMMIT`).
4. ☐ Repetir el diagnóstico del paso 2: debe devolver 0 filas en ambas consultas.

## 4. Migraciones (Liquibase, lanzadas a mano)

El backend en perfil `prod` **no** migra al arrancar (R6). Se usa la CLI oficial de Liquibase, la misma versión que el backend, con los changelogs de la versión desplegada. Los nombres de los changesets coinciden con los que guarda el backend.

```sh
# Vista previa: el SQL que se va a ejecutar, sin aplicarlo.
docker run --rm --network <red del legacy> --env-file deploy/secrets/migracion.env \
  -e INSTALL_MYSQL=true -v "$PWD/backend/src/main/resources:/liquibase/changelog:ro" \
  liquibase/liquibase:4.29.2 --search-path=/liquibase/changelog \
  --changelog-file=db/changelog/db.changelog-master.yaml update-sql

# Aplicar.
docker run ... (mismos parámetros) ... update
```

1. ☐ Revisar la salida de `update-sql` con el responsable y confirmar.
2. ☐ Ejecutar `update`.
3. ☐ Comprobar:
   - `SELECT ID FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED;` devuelve 8 changesets, de `001-guardias-cambios-columnas` a `005-guardias-medicos-unicidad`.
   - `SHOW TRIGGERS LIKE 'medicos';` devuelve `trg_medicos_tipo_insert` y `trg_medicos_tipo_update`.
   - `SHOW INDEX FROM guardias_medicos WHERE Key_name = 'UX_GUARDIA_MEDICO_DIA';` devuelve el índice.
   - `SELECT COUNT(*) FROM medicos_tipo_historico;` coincide con el número de médicos con `ID` no nulo.
4. Si algo falla:
   - Si se detiene un changeset por precondición (`HALT`, duplicados), se vuelve al paso 2.
   - Si falla el trigger por el binlog (error 1419), se resuelve según el paso 0 y se relanza `update`.

   Liquibase retoma desde el changeset que falló. En MySQL el DDL no es transaccional: lo aplicado antes del fallo se queda.
5. ☐ Si se activó `log_bin_trust_function_creators` solo para migrar, volver a dejarlo como estaba.

## 5. Desactivar la pantalla de cambios del legacy (R13)

1. ☐ Consultar y **anotar el valor actual**, que hará falta para la marcha atrás:
   ```sql
   SELECT * FROM configuracion WHERE config_key = 'ACTIVAR_CAMBIO_GUARDIAS' AND config_IdServicio = <servicio>;
   ```
2. ☐ Con confirmación del responsable, aplicar el cambio:
   - si la fila existe:
     ```sql
     UPDATE configuracion SET config_value = 'N'
      WHERE config_key = 'ACTIVAR_CAMBIO_GUARDIAS' AND config_IdServicio = <servicio>;
     ```
   - si no existe, crearla con `'N'`:
     ```sql
     INSERT INTO configuracion (config_key, config_value, config_IdServicio)
     VALUES ('ACTIVAR_CAMBIO_GUARDIAS', 'N', <servicio>);
     ```
3. ☐ Repetir para cada servicio que pase a usar la aplicación nueva.

## 6. Arranque

1. ☐ `docker compose -f deploy/docker-compose.yml up -d` (perfil `prod`).
2. ☐ `docker compose -f deploy/docker-compose.yml logs backend`: aparece `Started GuardiasApplication` y no hay errores de conexión.
3. ☐ Desplegar el legacy con el enlace a `/app/` del menú (T066, `web/common/navigation.jsp`).

## 7. Comprobación (humo)

En producción hay datos reales: **no se registran cambios de prueba**.

1. ☐ `https://<dominio>/app/` carga. `https://<dominio>/api/v1/auth/me` responde 401 con la cookie `XSRF-TOKEN` (`Secure; SameSite=Strict`).
2. ☐ Un administrador entra con su contraseña del legacy y ve el calendario de su servicio.
3. ☐ Un médico sin rol de administrador recibe «Prohibido» en el calendario del servicio.
4. ☐ Se abren «Ceder» e «Intercambiar» sobre una guardia futura y se ven los candidatos. Se **cancela**, sin registrar nada.
5. ☐ En el legacy, la solicitud de cambio de guardia muestra «El sistema no lo permite» (R13). El calendario del legacy se ve igual que antes.
6. ☐ El primer cambio real lo registra el administrador cuando negocio lo necesite. Se comprueba entonces en el calendario del legacy (FR-026).

El escenario completo del quickstart §2 se ejecuta en el entorno de pruebas con datos ficticios (T067), no en producción.

## 8. Marcha atrás

Los cambios de esquema son **aditivos**: columnas y tabla nuevas, clave primaria, índice único y triggers. El legacy funciona con ellos: lo comprueban las pruebas de regresión `LegacyVisibilidadIT` y `LegacyEquilibrioIT`. Por eso, para volver atrás **no hace falta deshacer el esquema**.

1. ☐ Parar el sistema nuevo: `docker compose -f deploy/docker-compose.yml down`.
2. ☐ Restaurar `ACTIVAR_CAMBIO_GUARDIAS` al valor anotado en el paso 5, o borrar la fila si no existía. Requiere confirmación del responsable.
3. ☐ Volver a desplegar el legacy sin el enlace a `/app/`.
4. Los cambios registrados con la aplicación nueva están en `guardias_cambios` y `guardias_medicos` con el formato del legacy. Siguen siendo válidos y visibles.

Si, aun así, hubiera que deshacer el esquema, se decide con el responsable. Hay dos caminos:
- **`rollback-count` de Liquibase.** Deshace los triggers (003) y el índice único (005), que tienen marcha atrás declarada. El changeset 004 (clave primaria) no la tiene: se deshace con SQL revisado aparte.
- **Restaurar la copia del paso 1.** Se pierde todo lo escrito después, también desde el legacy.
