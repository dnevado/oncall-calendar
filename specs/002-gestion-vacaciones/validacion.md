# Validación: funcionalidad 002

Registro de las comprobaciones de cierre (tasks.md, fase 6). Solo se anota como superado lo que se ha ejecutado (principio V).

## T051: validación automática del quickstart

**Fecha**: 2026-10-25. **Rama**: `rearchitecture-claude-based-speckit`.

**Entorno**: Windows 11, Maven 3.9.14, Java 21, Node 24.14.0, Docker 26.1.1 (Testcontainers con `mysql:5.7`). Datos ficticios (`DatosPrueba`) y reloj fijo (`IntegracionBase.HOY` = 2030-01-15).

| Comando | Resultado |
|---|---|
| `mvn -f backend/pom.xml verify` | BUILD SUCCESS. Surefire: 79 pruebas, 0 fallos. Failsafe: 89 pruebas, 0 fallos. Incluye todas las de la 001 |
| `npm --prefix frontend run gen:api` | Sin diferencias en `frontend/src/api/schema.d.ts` respecto al contrato 1.2.0 publicado |
| `npm --prefix frontend test` | 7 ficheros, 49 pruebas, 0 fallos |
| `npm --prefix frontend run lint` | 0 errores |
| `npm --prefix frontend run build` | Correcto. `index` 426 kB (133 kB comprimido) y `VacacionesPage` 88 kB (26 kB) en un trozo diferido; sin aviso de tamaño |
| `npx @redocly/cli@1.34.3 lint contracts/guardias-cambios.openapi.yaml` | Solo avisos de estilo (`security-defined`, `tag-description`, `info-license`, `operation-4xx-response`), los mismos tipos que en la 1.1.0 |

### Escenarios del quickstart

| # | Escenario | Prueba | Resultado |
|---|---|---|---|
| 1 | Añadir varios días | `ModificarVacacionesIT.escenario1_anadeVariosDiasYNoTocaLosDemas` | Superada |
| 2 | Conflicto con guardia | `ModificarVacacionesIT.escenario2_*`, `unaGuardiaEnOtroServicioNoImpideAnadir` | Superada |
| 3 | Días pasados y hoy | `ModificarVacacionesIT.escenario3_*`, `ReglasVacacionesTest.vac008_*` | Superada |
| 4 | Sin límite futuro e idempotencia | `ModificarVacacionesIT.escenario4_*`, `escenario5US1_*` | Superada |
| 5 | Quitar solo lo indicado | `ModificarVacacionesIT.escenarioUS2_1_*` | Superada |
| 6 | Mezcla con error | `ModificarVacacionesIT.escenarioUS2_3_*`, `unFalloAMitadDeOperacionNoGuardaNada` | Superada |
| 7 | Quitar un día pasado | `ModificarVacacionesIT.escenarioUS2_2_*` | Superada |
| 8 | Permisos | `ModificarVacacionesIT.escenario8_*`, `ConsultarVacacionesIT`, `ServicioAccessCoverageTest`, `VacacionesContractTest` | Superada |
| 9 | Resumen y conflictos | `ConsultarVacacionesIT` (incluidos los conflictos históricos), `ResumenVacaciones.test.tsx` | Superada |
| 10 | Concurrencia con una cesión | `ModificarVacacionesIT.cesionConfirmadaMientrasSeEsperaElBloqueo_*`, `vacacionesYCesionDelMismoDiaALaVez_soloUnaTieneExito` | Superada |
| 11 | CG-012 tras añadir | `ModificarVacacionesIT.escenario11_*` | Superada |
| 12 | Regresión legacy | `LegacyVacacionesIT` (altas y bajas con la SQL original de `VacacionesDBImpl` y `NoTieneVacaciones`) | Superada |
| 13 | Interfaz | `EditorVacaciones.test.tsx` (10 pruebas: ratón, teclado, periodo, días pasados, rechazo) y `ResumenVacaciones.test.tsx` (6 pruebas) | Superada en jsdom; falta la comprobación en navegador (T052) |

### Comprobación de que la prueba de concurrencia detecta el fallo

Antes del bloqueo del servicio se añadió temporalmente una lectura (`medicosDelServicio`). Así, MySQL fija la vista de lectura antes de esperar el bloqueo (hallazgo I1 del análisis).

Con ese cambio, `cesionConfirmadaMientrasSeEsperaElBloqueo_seVeAntesDeEvaluarVac003` **falla**: devuelve 200 en lugar de 422 y quedan a la vez la guardia y las vacaciones del mismo día. Se restauró el código y la prueba vuelve a pasar.

### Accesibilidad (T048)

Revisión de código y de contraste, sin lector de pantalla real:
- Cada día lleva un nombre accesible con su estado: de vacaciones, se añadirá, se quitará, conflicto o no editable.
- Además del color, cada estado tiene un símbolo: ✓, +, −, !.
- El foco pasa al editor al elegir un médico.

Contrastes calculados (WCAG 2.1 AA):

| Elemento | Contraste |
|---|---|
| Texto sobre los fondos de estado | 9,8 a 10,6 |
| Símbolos | 4,5 o más |
| Días pasados | 5,8, tras oscurecer el gris; antes era 3,77 |
| Foco | 6,6 con `--primario-oscuro`, que sustituye a `--foco` |

## T052: comprobación en el entorno local

**Fecha**: 2026-10-25. Arranque autorizado por el responsable.

**Entorno**:
- Copia local (`guardias-local-mysql-1`) con el backend actual en el puerto **8082**. El 8081 lo ocupaba un backend anterior a la 002 que no se pudo parar.
- Vite en el **5176**, apuntando al 8082. Del 5173 al 5175 había procesos de Vite antiguos.
- Usuario: el administrador ficticio de la 001 (`t067@prueba.local`, médico 9067). Solo se tocaron sus vacaciones, y se quitaron al final.
- Script: `scratchpad/t052.py`, que pasa por el proxy de Vite y no guarda en git datos personales ni la contraseña.

| Paso | Resultado |
|---|---|
| Inicio de sesión | 200 |
| Añadir un periodo futuro de 10 días (marzo de 2027) | 200, 10 añadidos. La consulta del legacy (`NoTieneVacaciones`, SQL literal de `VacacionesDBImpl`) ve los 10 días |
| Consulta del médico en marzo de 2027 | Los 10 días |
| Repetir la operación | 200, sin cambios |
| Julio y agosto de 2017 | **5 días en conflicto** señalados, los mismos que dio el descubrimiento |
| Quitar uno de esos días en conflicto | 422 `VAC-008`; el día sigue en la tabla |
| Resumen por defecto (de hoy a 6 meses) | 26 médicos, todos los del servicio 1 |
| Limpieza por la API | 10 quitados; el legacy deja de verlos |
| Copia intacta | `vacaciones_medicos` tiene 4.105 filas antes y después |

**Tiempos de respuesta** (por el proxy de Vite): las operaciones de vacaciones tardan de 16 a 159 ms (la más lenta, la consulta de todo el servicio en 2017). El inicio de sesión tarda 343 ms. Se cumple el objetivo de menos de 1 s del plan.

**Pendiente, solo en navegador** (no hecho):
- Recorrer la pantalla en `http://localhost:5176/app/servicios/1/vacaciones`.
- Cronometrar el registro de un periodo de 2 semanas (SC-001, menos de 1 minuto).
- Ver los días en el selector de la ficha del médico en el legacy (Tomcat). La parte de datos ya está comprobada arriba con su SQL.

## T053: revisión de migración

**Fecha**: 2026-10-25. **Alcance**: commits `cc867eb`..`90a1ae8` y las correcciones de esta revisión.

| Punto | Resultado |
|---|---|
| Alcance | Solo `backend/`, `frontend/`, `contracts/`, `docs/rules/` y `specs/002`. Ningún cambio en el legacy (`src/`, `web/`, `resources/`, `build/`, `bbdd/`) ni en el esquema. Única dependencia nueva: `react-day-picker` 9.14.0, aprobada |
| Reglas en el servidor | VAC-001, VAC-003 y VAC-008 se comprueban en el backend; la pantalla solo prepara la operación (FR-020) |
| Contrato | 1.2.0 solo añade; las respuestas se validan con `VacacionesContractTest` |
| Persistencia | MyBatis con SQL parametrizada; misma tabla y formato que el legacy; regresión con la SQL original |
| Despliegue | nginx y Spring no restringen métodos HTTP: el `PATCH` llega al backend. La CSP ya admite los estilos del selector |
| Seguridad | Sin secretos ni datos reales; pruebas con datos ficticios |
| Capacidades (principio VIII) | V1–V3, V5–V7, V9, M1–M3 cubiertas o sustituidas; V4 y V8 descartadas por spec; A1 y A2 siguen en el legacy |

**Hallazgos y correcciones:**
- **Importante (corregido).** El editor solo conocía los días guardados de los meses visibles y, mientras cargaban, los trataba como vacíos.
  - Efecto: un periodo que empezaba en un mes ya no visible contaba como nuevos días ya guardados.
  - Efecto: un día guardado pulsado durante la carga se apuntaba como «añadir».
  - Corrección: el editor recuerda los días de todos los meses cargados y bloquea la selección mientras carga.
  - Pruebas nuevas: `recuerda los días guardados de meses ya vistos…` y `no deja marcar días mientras se cargan…`. Ambas fallaban antes de la corrección.
- **Menor (corregido).** Después de guardar, el resumen pedía los datos dos veces en cada cambio de periodo.
- **Menor (fuera de alcance).** El token `--foco` de la 001 (`#66afe9`) tiene un contraste de 2,37:1 sobre blanco, por debajo del 3:1 de WCAG 1.4.11. Vacaciones usa `--primario-oscuro`. Queda como riesgo de la 001.

**Pruebas tras las correcciones:** `npm --prefix frontend test` 51/51, `lint` sin errores, `build` correcto. El backend no cambió: sigue siendo el resultado de T051.
