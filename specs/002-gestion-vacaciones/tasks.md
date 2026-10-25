---

description: "Lista de tareas de la funcionalidad 002: gestión de vacaciones de los médicos"
---

# Tasks: Gestión de vacaciones de los médicos

**Input**: Design documents from `/specs/002-gestion-vacaciones/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/guardias-cambios.openapi.yaml), [quickstart.md](./quickstart.md)

**Tests**: obligatorios. La constitución (principio V) exige pruebas ejecutadas para cada regla y para la convivencia con el legacy (research V9). Se escriben antes que el código y deben fallar primero.

**Organization**: tareas agrupadas por historia de usuario. US1 y US2 son P1 y forman juntas el MVP; US3 es P2.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede hacer en paralelo (ficheros distintos, sin dependencias pendientes).
- **[Story]**: historia a la que pertenece (US1, US2, US3).
- Rutas: `backend/src/main/java/es/medoncalls/guardias/` se abrevia como `…/guardias/` y `backend/src/test/java/es/medoncalls/guardias/` como `test/…/guardias/`.

## Decisiones de descomposición

- **Consulta en la fase fundacional.** `GET /servicios/{servicioId}/vacaciones` (con conflictos) va en la fase 2: el editor de US1 y US2 la necesita para marcar los días guardados (FR-018) y el resumen de US3 la reutiliza.
- **Códigos de regla.** `VAC-003` y `VAC-008` se añaden al enum existente `…/guardias/cambios/domain/ReglaCodigo.java`, porque el enum `Infraccion.regla` del contrato es único y `ProblemDetailsHandler` ya lo serializa. Se reutilizan `Infraccion` y `ReglasIncumplidasException` de `cambios`; la 001 no cambia de comportamiento.
- **PATCH por partes.** US1 implementa la lista `anadir` y US2 la lista `quitar` del mismo `PATCH`. Hasta terminar US2, el servicio rechaza con 400 una petición con `quitar` no vacía (nunca la ignora en silencio). El MVP desplegable es US1 + US2.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: contrato publicado, tipos generados y dependencia nueva (principio IV, research V6).

- [X] T001 Publicar el contrato: copiar `specs/002-gestion-vacaciones/contracts/guardias-cambios.openapi.yaml` sobre `contracts/guardias-cambios.openapi.yaml` con `info.version: 1.2.0` (sin `-draft`); comprobar con `git diff contracts/` que solo añade (tag `vacaciones`, dos rutas, esquemas `ModificarVacacionesRequest`, `ResultadoVacaciones`, `VacacionesMedico`, `DiaVacaciones`, y `VAC-003`/`VAC-008` en `Infraccion.regla`) y pasar `npx @redocly/cli lint contracts/guardias-cambios.openapi.yaml` (solo se admiten los avisos `security-defined` que ya tenía la 1.1.0)
- [X] T002 Regenerar los tipos con `npm --prefix frontend run gen:api` (actualiza `frontend/src/api/schema.d.ts`) y exportar en `frontend/src/api/client.ts` los alias `VacacionesMedico`, `DiaVacaciones`, `ModificarVacacionesRequest` y `ResultadoVacaciones`, igual que los alias existentes (`Guardia`, `Cambio`…); `npm --prefix frontend run build` sin errores (depende de T001)
- [X] T003 [P] Añadir la dependencia `react-day-picker` en versión exacta: `npm --prefix frontend install --save-exact react-day-picker@9.14.0`; comprobar en `frontend/package.json` que queda `"react-day-picker": "9.14.0"` sin `^`, y con `npm --prefix frontend ls react-day-picker date-fns` y el campo `license` de `frontend/node_modules/react-day-picker/package.json` y `frontend/node_modules/date-fns/package.json` que ambas son MIT (plan, Complexity Tracking)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: códigos de regla, acceso a datos, apoyo de pruebas y consulta de vacaciones del servicio.

**⚠️ CRITICAL**: ninguna historia empieza antes de terminar esta fase.

### Pruebas y apoyo (primero)

- [X] T004 [P] Ampliar `test/…/guardias/support/DatosPrueba.java` con `List<LocalDate> diasVacaciones(long medicoId)` (lee `vacaciones_medicos` ordenado por `FDay`) y `int contarVacaciones(long medicoId)`; reutiliza el `vacaciones(medicoId, dia)` existente para sembrar y datos ficticios (principio VI)
- [X] T005 [P] Ampliar `test/…/guardias/legacy/LegacySql.java` con **copias literales** de la SQL del legacy, cada una con su referencia: consulta de un día y de un rango de `src/com/guardias/database/VacacionesDBImpl.java:15` y `:183-207`, meses con vacaciones `SQLGENERAL_MESES` (`:17`), recuento (`:19`, `:158-162`) y la consulta de `ProcesarMedicos.NoTieneVacaciones` (`src/com/guardias/ProcesarMedicos.java:2505-2518`). Donde el legacy concatena valores, la copia marca los huecos y la prueba los rellena con datos ficticios; no se cambia la forma de la SQL (FR-016, SC-006)
- [X] T006 [P] Prueba de integración de la consulta en `test/…/guardias/integration/ConsultarVacacionesIT.java` (extiende `IntegracionBase`): `GET /api/v1/servicios/{id}/vacaciones?desde&hasta` devuelve todos los médicos del servicio, también el inactivo en el servicio (`activo=false`) y los que no tienen días, ordenados por apellidos y nombre, con sus días ordenados; filtro `medicoId`; un día con guardia del médico en el servicio lleva `conflicto` con la guardia y uno con guardia en otro servicio no; días pasados incluidos; 400 si `hasta < desde` o el rango supera 731 días; 404 si `medicoId` no pertenece al servicio; 403 a un médico que no administra el servicio (FR-001, FR-002, FR-012 a FR-014, research V5 y V7)
- [X] T007 [P] Prueba de contrato de la consulta en `test/…/guardias/contract/VacacionesContractTest.java` con `OpenApiContract.cumple()`/`respuestaCumple()`: respuestas 200, 400, 403 y 404 de `listarVacaciones` cumplen `contracts/guardias-cambios.openapi.yaml` (principio IV)

### Implementación

- [X] T008 Añadir `VAC_003("VAC-003")` y `VAC_008("VAC-008")` a `…/guardias/cambios/domain/ReglaCodigo.java`, y sus textos en `backend/src/main/resources/messages_es.properties` y `messages.properties` con la misma convención de claves que las reglas CG (VAC-008: «El día {fecha} es anterior a hoy y no se puede modificar»; VAC-003: «El médico tiene una guardia de {tipo} el {fecha} en este servicio»), sin datos personales. `ReglasVacaciones` los obtiene con la interfaz `…/guardias/cambios/domain/Mensajes.java`, con las claves `regla.VAC-003` y `regla.VAC-008`, como las reglas CG
- [X] T008b Dar a `…/guardias/cambios/application/ReglasIncumplidasException.java` un constructor con el texto del rechazo (el actual conserva el de la 001) y hacer que `…/guardias/common/ProblemDetailsHandler.java` (`reglasIncumplidas`) lo use en el `detail`; vacaciones usará «No se han guardado las vacaciones porque incumplen las reglas indicadas». `CambiosContractTest` y `RegistrarCesionIT` siguen en verde (SC-004)
- [X] T009 Crear `…/guardias/vacaciones/infrastructure/VacacionMapper.java` y `backend/src/main/resources/mappers/VacacionMapper.xml` con SQL parametrizada (`#{}`, principio VII) y fechas como texto `yyyy-MM-dd` (`FechaLegacy`): `boolean perteneceAlServicio(servicioId, medicoId)` (fila en `guardias_servicios_medicos`, sin mirar `ActivoServicio`); `List<MedicoServicioFila> medicosDelServicio(servicioId, medicoId?)` (`medicos.ID`, `Nombre`, `Apellidos`, `guardias_servicios_medicos.ActivoServicio`, ordenados por apellidos y nombre); `List<DiaVacacionFila> diasConConflicto(servicioId, desde, hasta, medicoId?)` con `LEFT JOIN guardias_medicos` por médico, `LEFT(FGuardia,10) = FDay` e `IdServicio` (research V5). El `VacacionesMapper` de `cambios` no se toca (research V1)
- [X] T010 Crear el caso de uso de consulta `…/guardias/vacaciones/application/ConsultarVacacionesService.java`: valida el rango (`hasta >= desde`, como mucho 731 días → `PeticionInvalidaException`), 404 (`RecursoNoEncontradoException`) si `medicoId` no pertenece al servicio, y agrupa los días por médico con su conflicto como `Guardia` (depende de T009)
- [X] T011 Crear `…/guardias/vacaciones/api/VacacionesController.java` con `GET /api/v1/servicios/{servicioId}/vacaciones` (`desde`, `hasta` obligatorios, `medicoId` opcional) y los DTO en `…/guardias/vacaciones/api/dto/` (`VacacionesMedicoDto`, `DiaVacacionesDto`, reutilizando `GuardiaDto` y `MedicoResumenDto` de `cambios`, que el contrato ya usa como `Guardia` y `MedicoResumen`); primera instrucción `servicioAccess.exigirAdministrador(usuario, servicioId)` (FR-001; depende de T010)
- [X] T012 Ejecutar `mvn -f backend/pom.xml verify -Dtest=VacacionesContractTest -Dit.test=ConsultarVacacionesIT -Dsurefire.failIfNoSpecifiedTests=false` (surefire ejecuta `*Test` y failsafe `*IT`) y confirmar que T006 y T007 pasan, y que `ServicioAccessCoverageTest` incluye ya `GET /api/v1/servicios/{servicioId}/vacaciones`; añadir esa ruta a su `assertThat(probadas).contains(...)` en `test/…/guardias/integration/ServicioAccessCoverageTest.java`
- [X] T013 [P] Añadir en `frontend/src/test/server.ts` datos ficticios de vacaciones (`VacacionesMedico[]` con un médico inactivo, uno sin días y un día con `conflicto`) y el manejador MSW de `GET /api/v1/servicios/:id/vacaciones`, con la forma del contrato
- [X] T014 [P] Crear `frontend/src/features/vacaciones/useVacaciones.ts` con la consulta (`listarVacaciones(servicioId, desde, hasta, medicoId?)` vía `openapi-fetch`), estados de carga y error como `useRegistrarCambio.ts`, y utilidades de fecha en local (`yyyy-MM-dd`, sin desfase de zona) reutilizando `frontend/src/features/cambios/fechas.ts` donde sirva

**Checkpoint**: consulta disponible en el servidor y en el cliente; la 001 sigue en verde.

---

## Phase 3: User Story 1 - Añadir días de vacaciones a un médico (Priority: P1) 🎯 MVP (con US2)

**Goal**: el administrador marca varios días de un médico y los guarda; el servidor aplica VAC-003 y VAC-008 y solo añade esos días.

**Independent Test**: con datos de prueba, añadir varios días y comprobar que quedan registrados, que CG-012 los tiene en cuenta y que la SQL del legacy los ve; intentar días pasados o con guardia y comprobar el rechazo completo (quickstart, escenarios 1 a 4, 8, 10 a 12).

### Tests for User Story 1 ⚠️

> Escribirlas primero y comprobar que fallan.

- [X] T015 [P] [US1] Pruebas unitarias en `test/…/guardias/vacaciones/domain/ReglasVacacionesTest.java`: VAC-008 rechaza días de `anadir` anteriores a hoy y acepta hoy; sin límite futuro (fecha a 3 años, VAC-004); VAC-003 da una infracción por cada día con guardia del médico en el servicio, con la guardia, y todas a la vez (FR-011); guardias de otro servicio no cuentan (FR-010); cálculo de `anadidos` = `anadir` − existentes (FR-007)
- [X] T016 [P] [US1] Pruebas unitarias en `test/…/guardias/vacaciones/domain/ModificacionVacacionesTest.java` de la forma de la petición: listas vacías, fecha en las dos listas, más de 366 fechas por lista y fechas repetidas dentro de una lista → error de petición (400, research V7)
- [X] T017 [P] [US1] Pruebas de integración en `test/…/guardias/integration/ModificarVacacionesIT.java` para `anadir` (quickstart 1 a 4 y 8): 13 días futuros → 200 con `anadidos`=13 y el resto intacto (SC-002); conflicto con guardia el día 10 → 422 `VAC-003` con esa fecha y 0 días añadidos; día pasado → 422 `VAC-008`, hoy → 200; repetir la misma petición → 200 con `anadidos` vacío y una sola fila (FR-007); médico sin rol → 403 y médico de otro servicio → 404, sin cambios (SC-005); el `detail` del 422 menciona las vacaciones, no el cambio (SC-004, T008b); reloj fijado con `RelojPrueba`
- [X] T018 [P] [US1] Prueba de concurrencia en `ModificarVacacionesIT.java` (quickstart 10): en dos hilos, añadir vacaciones a B el día D y registrar la cesión a B de una guardia del día D (`POST …/cambios` de la 001); exactamente una tiene éxito y nunca quedan las dos (research V3). El orden se fuerza con un `CountDownLatch`: la cesión se confirma mientras la operación de vacaciones espera el bloqueo, y se comprueba que el resultado final no tiene a la vez la guardia y las vacaciones de D. Y CG-012 tras añadir (quickstart 11): añadir el día D y después intentar la cesión → 422 `CG-012` (FR-015, US1-6)
- [X] T019 [P] [US1] Prueba de contrato de `modificarVacaciones` en `test/…/guardias/contract/VacacionesContractTest.java`: petición y respuestas 200, 400, 403, 404 y 422 (con `VAC-003` y `VAC-008`) cumplen el contrato
- [X] T020 [P] [US1] Regresión legacy en `test/…/guardias/legacy/LegacyVacacionesIT.java` (quickstart 12, parte de altas): tras añadir días por la API, la SQL de `LegacySql` (día, rango, meses, recuento y `NoTieneVacaciones`) ve exactamente esos días, con `FDay` en formato `yyyy-MM-dd` (FR-016, SC-006)
- [X] T021 [US1] Ampliar `test/…/guardias/integration/ServicioAccessCoverageTest.java` para enviar también un cuerpo JSON válido en `PATCH` (`{"anadir":["2030-02-15"],"quitar":[]}`) y exigir `PATCH /api/v1/servicios/{servicioId}/medicos/{medicoId}/vacaciones` en la lista de operaciones probadas (FR-001)

### Implementation for User Story 1

- [X] T022 [P] [US1] Crear `…/guardias/vacaciones/domain/ModificacionVacaciones.java`: recibe `anadir` y `quitar` como `List<LocalDate>` y comprueba la forma de T016 (listas no vacías, sin repetidos en una lista, ninguna fecha en las dos, como mucho 366 por lista), lanzando `PeticionInvalidaException`; después expone las fechas como conjuntos. El DTO de T026 también usa `List` para no perder los repetidos al deserializar
- [X] T023 [P] [US1] Crear `…/guardias/vacaciones/domain/ReglasVacaciones.java`: dada la modificación, «hoy» y las guardias del médico en el servicio en esas fechas, devuelve todas las `Infraccion` (VAC-008 y VAC-003 sobre `anadir`) con sus textos de `Mensajes` (T008); sin acceso a datos ni reloj propio (research V8)
- [X] T024 [US1] Añadir a `VacacionMapper` (`.java` y `.xml`): `guardiasEnFechas(servicioId, medicoId, fechas)` (`LEFT(FGuardia,10) IN (...)` e `IdServicio`), `diasExistentes(medicoId, fechas)` e `insertarDias(medicoId, fechas)` con `INSERT … ON DUPLICATE KEY UPDATE FDay = FDay` sobre `IND_VACACIONES_01` (research V3)
- [X] T025 [US1] Crear `…/guardias/vacaciones/application/ModificarVacacionesService.java` (`@Transactional`) en el orden de data-model: validación de forma; abrir la transacción con `guardiaMapper.lockServicio(servicioId)` como primera sentencia (ninguna consulta antes, para que las lecturas posteriores vean lo ya confirmado, como en `RegistrarCambioService.java:71-74`); 404 si el médico no pertenece al servicio; reglas con `LocalDate.now(clock)`; si hay infracciones, `ReglasIncumplidasException` sin escribir nada (FR-006); si no, insertar `anadir` − existentes y devolver `anadidos`. Mientras no exista US2, `quitar` no vacía → `PeticionInvalidaException` (depende de T022 a T024)
- [X] T026 [US1] Añadir a `VacacionesController.java` el `PATCH /api/v1/servicios/{servicioId}/medicos/{medicoId}/vacaciones` con `ModificarVacacionesRequestDto` y `ResultadoVacacionesDto` en `…/guardias/vacaciones/api/dto/`; `exigirAdministrador` primero; fechas inválidas en el JSON → 400 por `ProblemDetailsHandler` (como en la 001, un cuerpo ilegible da 400 antes de comprobar el rol; con cuerpo válido el rol se comprueba siempre); al lanzar `ReglasIncumplidasException` usa el texto de vacaciones de T008b (depende de T025)
- [X] T027 [US1] Ejecutar `mvn -f backend/pom.xml verify` y confirmar que T015 a T021 pasan, y que siguen pasando las pruebas de la 001 (`RegistrarCesionIT`, `LegacyVisibilidadIT`…)
- [X] T028 [P] [US1] Añadir en `frontend/src/test/server.ts` el manejador MSW de `PATCH …/medicos/:medicoId/vacaciones` (200 con `anadidos`, y 422 con `VAC-003` y `VAC-008` según la fecha)
- [X] T029 [P] [US1] Pruebas en `frontend/src/features/vacaciones/EditorVacaciones.test.tsx` (Testing Library y `user-event`): el calendario empieza en lunes y está en español; los días guardados aparecen marcados; los días pasados no se pueden marcar; marcar varios días con ratón y con teclado (flechas y Enter/Espacio) muestra «añadir N» antes de guardar; guardar envía solo las fechas nuevas en `anadir`; tras 200 se recargan los días; un 422 muestra cada fecha con su motivo en una región `role="alert"` (FR-011, FR-018 a FR-020)
- [X] T030 [US1] Crear `frontend/src/features/vacaciones/EditorVacaciones.tsx` con `DayPicker` de `react-day-picker` (`mode="multiple"`, `weekStartsOn={1}`, `locale={es}` de `react-day-picker/locale`, `disabled={{ before: hoy }}`, un mes en ancho estrecho y dos en escritorio): estado de días a añadir frente a guardados, resumen de cambios, botón Guardar que llama a `modificarVacaciones` de `useVacaciones.ts`, aviso de error de reglas reutilizando `frontend/src/features/cambios/Rechazo.tsx` si encaja. Los días guardados y los nuevos se distinguen también con texto o icono, no solo con color (FR-019). El servidor decide: la pantalla no valida conflictos por su cuenta (FR-020)
- [X] T031 [US1] Añadir `modificarVacaciones(servicioId, medicoId, {anadir, quitar})` a `frontend/src/features/vacaciones/useVacaciones.ts`, devolviendo el resultado o las infracciones del 422
- [X] T032 [US1] Crear `frontend/src/features/vacaciones/VacacionesPage.tsx` (selector de médico del servicio a partir de la consulta y el editor del elegido) y `frontend/src/features/vacaciones/vacaciones.css` con los tokens ya definidos (sin colores sueltos; estilos de `react-day-picker/style.css` importados y ajustados con sus variables CSS); en `frontend/src/App.tsx`, ruta `/servicios/:servicioId/vacaciones`, `pagina: 'vacaciones'` en `PaginaDelServicio` y enlace «Vacaciones» en la navegación del servicio
- [X] T033 [US1] Ejecutar `npm --prefix frontend test`, `npm --prefix frontend run lint` y `npm --prefix frontend run build`, y confirmar que pasan

**Checkpoint**: se pueden añadir días de extremo a extremo; no desplegar sin US2.

---

## Phase 4: User Story 2 - Quitar días de vacaciones (Priority: P1) 🎯 MVP (con US1)

**Goal**: el administrador desmarca días y los guarda; solo esos días se quitan, nunca días pasados, y una operación mixta es atómica.

**Independent Test**: quitar algunos días y comprobar que solo esos desaparecen; intentar quitar un día pasado; mezcla de quitar y añadir con un conflicto → no cambia nada (quickstart, escenarios 5 a 7 y 12).

### Tests for User Story 2 ⚠️

- [X] T034 [P] [US2] Ampliar `ReglasVacacionesTest.java`: VAC-008 también para `quitar` (día pasado → infracción; hoy permitido); `quitados` = `quitar` ∩ existentes; quitar un día que no existe no es error (FR-007)
- [X] T035 [P] [US2] Ampliar `ModificarVacacionesIT.java` (quickstart 5 a 7): quitar dos días de un periodo → solo esos desaparecen (FR-005, SC-002); quitar un día pasado → 422 `VAC-008` y el día sigue; quitar un día válido y añadir uno con guardia → 422 y no se quita ni se añade nada (FR-006); quitar un día futuro en conflicto con una guardia → 200 (spec, Edge Cases); dos administradores: uno añade un día y otro quita otro; ninguno pisa al otro (Edge Cases)
- [X] T036 [P] [US2] Ampliar `LegacyVacacionesIT.java` (quickstart 12, parte de bajas): tras quitar días por la API, la SQL del legacy y `NoTieneVacaciones` dejan de verlos y siguen viendo los demás

### Implementation for User Story 2

- [X] T037 [US2] Ampliar `ReglasVacaciones.java` con VAC-008 sobre `quitar`, y `VacacionMapper` (`.java` y `.xml`) con `borrarDias(medicoId, fechas)` por clave (`IdMedico` y `FDay IN (...)`)
- [X] T038 [US2] Completar `ModificarVacacionesService.java`: quitar la restricción temporal sobre `quitar` de T025, evaluar las reglas de las dos listas antes de escribir, borrar `quitar` ∩ existentes en la misma transacción y devolver `quitados` (depende de T037)
- [X] T039 [US2] Ejecutar `mvn -f backend/pom.xml verify` y confirmar que T034 a T036 pasan junto con las de US1 y la 001
- [X] T040 [US2] Ampliar `EditorVacaciones.test.tsx` y `EditorVacaciones.tsx`: desmarcar días guardados (ratón y teclado) los pone como «a quitar», distinguidos sin depender del color; el resumen muestra «añadir N, quitar M»; guardar envía `quitar` y `anadir` juntos; volver a marcar un día «a quitar» lo devuelve a guardado; días pasados guardados visibles pero no editables (FR-012, FR-019)
- [X] T041 [US2] (MAY, FR-018) Añadir al editor la elección de un periodo de días seguidos (`mode="range"` o un conmutador «días sueltos / periodo») que se convierte en días sueltos de `anadir`; con su prueba en `EditorVacaciones.test.tsx`. Se puede posponer sin afectar al MVP; si se pospone, dejar constancia en el informe final
- [X] T042 [US2] Ejecutar `npm --prefix frontend test`, `lint` y `build`, y confirmar que pasan

**Checkpoint**: MVP completo (US1 + US2): se añaden y quitan días de forma atómica, con las reglas en el servidor.

---

## Phase 5: User Story 3 - Ver las vacaciones del servicio y los conflictos (Priority: P2)

**Goal**: resumen de las vacaciones del servicio (por defecto, hoy + 6 meses, navegable) con los conflictos señalados.

**Independent Test**: con un día de vacaciones con guardia del mismo médico, abrir el resumen y comprobar que aparecen todos los médicos y que ese día está señalado; un médico sin rol no puede ver nada (quickstart, escenario 9).

### Tests for User Story 3 ⚠️

- [ ] T043 [P] [US3] Pruebas en `frontend/src/features/vacaciones/ResumenVacaciones.test.tsx`: por defecto pide de hoy a hoy + 6 meses; muestra todos los médicos, también el inactivo (señalado con texto «inactivo en el servicio», no solo color) y los que no tienen días; agrupa los días por mes; un día en conflicto aparece señalado con texto y la guardia; navegar a periodos anteriores y siguientes pide el nuevo rango; elegir un médico abre su editor (FR-012 a FR-014, FR-019, M1 a M3)
- [ ] T044 [P] [US3] Añadir a `ConsultarVacacionesIT.java` el caso de los conflictos históricos: con datos ficticios que reproducen la forma de los 5 conflictos de 2017 (días pasados con guardia del mismo médico en el servicio), la consulta de julio y agosto de ese año los devuelve todos con `conflicto` y el `PATCH` sobre ellos da 422 `VAC-008` (SC-007, P7: se muestran, no se corrigen)

### Implementation for User Story 3

- [ ] T045 [US3] Crear `frontend/src/features/vacaciones/ResumenVacaciones.tsx`: tabla o lista accesible (encabezados, `scope`) por médico con sus días agrupados por mes, conflictos marcados con texto e icono y la guardia en un texto accesible; navegación de periodo (anterior, siguiente, volver a hoy) con rango de 6 meses
- [ ] T046 [US3] Integrar el resumen en `VacacionesPage.tsx`: resumen arriba (o panel lateral en escritorio) y editor del médico elegido; en el editor, los días en conflicto también aparecen señalados (FR-014); estilos en `vacaciones.css` con tokens
- [ ] T047 [US3] Ejecutar `npm --prefix frontend test`, `lint` y `build`, y `mvn -f backend/pom.xml verify`, y confirmar que pasan

**Checkpoint**: las tres historias funcionan y se pueden probar por separado.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T048 [P] Accesibilidad (WCAG 2.1 AA, FR-019): revisar con teclado la página completa (orden de foco, foco visible, anuncios de `role="alert"` y `aria-live`), contraste de los estados del calendario con los tokens y textos alternativos de los iconos; corregir en `vacaciones.css`, `EditorVacaciones.tsx` y `ResumenVacaciones.tsx`
- [ ] T049 [P] Comprobar que la CSP de `frontend/nginx.conf` admite los estilos de `react-day-picker` sin ampliarla; si hiciera falta un cambio, documentarlo y pedir aprobación antes (restricción: no cambiar despliegue fuera de alcance)
- [ ] T050 [P] Actualizar documentación: `docs/rules/vacaciones-reglas-candidatas.md` (VAC-001 a VAC-012 con la referencia a su implementación y a sus pruebas), `deploy/README.md` si cambia algo del despliegue, y la tabla de capacidades de `specs/002-gestion-vacaciones/plan.md` con el estado final
- [ ] T051 Ejecutar la validación completa del quickstart: `mvn -f backend/pom.xml verify`, `npm --prefix frontend run gen:api` (sin diferencias), `test`, `lint` y `build`; registrar comandos y resultados reales en `specs/002-gestion-vacaciones/validacion.md` (mismo formato que `specs/001-registro-cambios-guardia/validacion.md`)
- [ ] T052 Comprobación manual en el entorno local (quickstart, «Comprobación manual»; requiere backend en 8081, Vite y la copia local, que se arrancan solo con permiso del usuario): añadir un periodo con `t067@prueba.local`, verlo en la ficha del legacy apuntando a la misma base, y ver los 5 conflictos de julio y agosto de 2017 señalados y no editables; cronometrar el registro de un periodo de 2 semanas desde que se abre la pantalla (SC-001, menos de 1 minuto) y anotar los tiempos de las respuestas de consulta y modificación en las herramientas del navegador (objetivo del plan, menos de 1 s); registrar el resultado en `validacion.md` sin datos personales (solo agregados)
- [ ] T053 Ejecutar `/migration-review` sobre el incremento 002 (capacidades V1 a V9, M1 a M3, A1 y A2 del plan; reglas VAC; contrato 1.2.0) y corregir los hallazgos bloqueantes e importantes
- [ ] T054 Informe de cierre (CLAUDE.md, «Finalización»): cambios, pruebas ejecutadas con resultados, desviaciones (incluida T041 si se pospone), riesgos (R1 aceptado; verificación en móvil aplazada según H4 de la 001, hasta el 2027-04-30) y decisiones pendientes

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (fase 1)**: sin dependencias. T002 depende de T001.
- **Foundational (fase 2)**: depende de la fase 1 y bloquea las historias. T008, T008b y T009 primero; T010 → T011 → T012. T013 y T014 dependen de T002.
- **US1 (fase 3)**: depende de la fase 2.
- **US2 (fase 4)**: depende de US1, porque completa el mismo servicio (`ModificarVacacionesService`) y el mismo editor.
- **US3 (fase 5)**: en el backend depende de la fase 2 (la consulta ya existe), salvo T044, que usa el `PATCH` y depende de T026 (US1). En el frontend, T046 integra con `VacacionesPage.tsx` de US1.
- **Polish (fase 6)**: depende de las historias que se entreguen.

### Within Each User Story

- Pruebas escritas y en rojo antes de implementar.
- Dominio → mapper → servicio → controlador → frontend.
- Cada historia termina con la ejecución de sus pruebas (T027, T033, T039, T042, T047): no se marca completa sin ejecutarlas.

### Parallel Opportunities

- Fase 1: T003 en paralelo con T001 y T002.
- Fase 2: T004 a T007 en paralelo; T013 y T014 en paralelo entre sí y con la implementación del backend.
- US1: T015 a T020 en paralelo; T022 y T023 en paralelo; T028 y T029 en paralelo con el backend.
- US2: T034 a T036 en paralelo.
- US3: T043 y T044 en paralelo; el frontend de US3 se puede hacer a la vez que el backend de US2.
- Polish: T048 a T050 en paralelo.

---

## Parallel Example: User Story 1

```bash
# Pruebas de US1 a la vez (todas deben fallar al principio):
Task: "Unitarias VAC-003/VAC-008 en test/…/guardias/vacaciones/domain/ReglasVacacionesTest.java"
Task: "Forma de la petición en test/…/guardias/vacaciones/domain/ModificacionVacacionesTest.java"
Task: "Integración de anadir en test/…/guardias/integration/ModificarVacacionesIT.java"
Task: "Contrato de modificarVacaciones en test/…/guardias/contract/VacacionesContractTest.java"
Task: "Regresión legacy en test/…/guardias/legacy/LegacyVacacionesIT.java"

# Dominio a la vez:
Task: "ModificacionVacaciones.java en …/guardias/vacaciones/domain/"
Task: "ReglasVacaciones.java en …/guardias/vacaciones/domain/"
```

---

## Implementation Strategy

### MVP (US1 + US2)

1. Fase 1 (contrato 1.2.0, tipos, dependencia).
2. Fase 2 (códigos, mapper, consulta, apoyo de pruebas).
3. US1 → validar con T027 y T033.
4. US2 → validar con T039 y T042. **Aquí el MVP es desplegable**: sustituye al selector de vacaciones de la ficha legacy.
5. Parar y revisar con el usuario antes de seguir.

### Incremental Delivery

1. MVP (US1 + US2).
2. US3: resumen del servicio y conflictos (paridad con la columna «Vacaciones» del listado legacy).
3. Polish: accesibilidad, quickstart, comprobación manual, `/migration-review` e informe.

## Notes

- Nada de esta funcionalidad modifica el legacy, el esquema ni los datos existentes (FR-016 opción C, research V4).
- Sin datos personales en pruebas, fixtures ni documentos; las cifras de la copia local solo como agregados (principio VI).
- Commit por tarea o por grupo lógico; sin push salvo petición expresa.
- No declarar pruebas superadas sin ejecutarlas.
