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

## T052: comprobación manual en el entorno local

**Pendiente.** Necesita el backend en 8081 y Vite sobre la copia local, que solo se arrancan con permiso del responsable.
