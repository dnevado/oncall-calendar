# Gestión de guardias — instrucciones para Claude Code

## Objetivo
Modernizar incrementalmente una aplicación legacy JavaScript/jQuery, JSP, Java, iBATIS y MySQL hacia React + TypeScript y Spring Boot, con una interfaz moderna que conserve las capacidades que los usuarios ya tienen (ver «Interfaz y experiencia de usuario»).

## Antes de trabajar
1. Lee la constitución: `.specify/memory/constitution.md`. Prevalece sobre este fichero.
2. Identifica la especificación activa en `.specify/feature.json` y lee su carpeta en `specs/` (spec, plan, tasks, contratos).
3. Lee la documentación pertinente: `docs/legacy/` (descubrimiento), `docs/rules/` (catálogo de reglas) y `deploy/README.md`.
4. Revisa `git status` y no sobrescribas cambios del usuario.
5. Inspecciona el build antes de proponer comandos.

## Mapa del repositorio
| Ruta | Contenido |
|---|---|
| `src/`, `web/`, `resources/`, `build/`, `bbdd/` | Legacy (solo lectura salvo excepción aprobada en un plan) |
| `web/inicio.jsp`, `web/js/guardias.js`, `web/css/custom.css` | Calendario legacy (FullCalendar 3) y sus estilos |
| `backend/` | Spring Boot 3 + Java 21, MyBatis, Liquibase (Maven) |
| `frontend/` | React 18 + TypeScript + Vite, servido en `/app/` |
| `contracts/` | Contratos OpenAPI publicados (fuente para `npm run gen:api`) |
| `deploy/` | Docker Compose, nginx y secretos (estos, fuera de git) |
| `specs/`, `.specify/` | Spec Kit: especificaciones, plantillas y constitución |
| `docs/legacy/`, `docs/rules/` | Evidencias legacy y reglas candidatas/validadas |
| `jsp_inventory*.csv` | Inventario de JSP, scriptlets y mapeos |

## Comandos
- Backend: `mvn -f backend/pom.xml verify` (las pruebas de integración usan Testcontainers y requieren Docker).
- Frontend: `npm --prefix frontend test`, `npm --prefix frontend run lint`, `npm --prefix frontend run build`.
- Tipos del contrato: `npm --prefix frontend run gen:api` tras cambiar `contracts/`.
- Despliegue local: ver `deploy/README.md` y `deploy/local/README.md`.

## Fuentes de verdad
- Requisitos: especificación aprobada de Spec Kit.
- Reglas funcionales: catálogo versionado y validado por negocio (`docs/rules/`).
- Evidencias legacy: código y datos de prueba autorizados.
- Contratos: OpenAPI versionado en `contracts/`.
- Capacidades de la interfaz: pantallas legacy (qué permite hacer cada una, no cómo se ve).
- Comportamiento implementado: código y pruebas ejecutadas.

## Interfaz y experiencia de usuario
Los widgets legacy pueden sustituirse por componentes y comportamientos modernos de React, con un aspecto actual. Lo que no se puede perder es la **capacidad** que ofrecen: el widget se puede cambiar, la función no.

- **Capacidades que se conservan** (lista mínima, ampliable con el inventario de `/legacy-discovery`):
  - Calendario de guardias (legacy: `web/inicio.jsp`, FullCalendar 3.0): ver las guardias por año, mes, semana y día; navegar entre periodos y volver a hoy; ir de un día o semana a su detalle; ver los días con muchas guardias sin perder información; distinguir festivos y días especiales; fechas en español.
  - Mover o reasignar guardias de forma directa (legacy: arrastrar y soltar en el calendario y en la zona de guardias externas).
  - Elegir varias fechas de vacaciones a la vez (legacy: MultiDatesPicker en `web/medico/detallemedico.jsp`).
  - Elegir fechas en formularios e informes (legacy: datepicker de jQuery UI en español).
- **Libertad de diseño.** Se puede proponer otra disposición, interacción o estética (por ejemplo, paneles laterales, filtros, atajos, vistas adaptadas a móvil) si mantiene esas capacidades y es igual o más fácil de usar. Usa componentes maduros (p. ej. FullCalendar 6 para React) antes que construirlos a mano; toda dependencia nueva se justifica en el plan.
- **Calidad visual.** Interfaz moderna, coherente, accesible (WCAG 2.1 AA) y adaptable a móvil, con un sistema de estilos (tokens de color, tipografía y espaciado) en lugar de estilos sueltos. El CSS legacy es una referencia, no una obligación.
- **Reglas en el servidor.** Arrastrar y soltar o cualquier otro atajo de la interfaz solo inicia la operación: el backend valida y aplica las reglas (principio II).
- **Desviaciones.** Eliminar una capacidad legacy es una decisión explícita, registrada en la especificación y aprobada; cambiar solo el widget o el aspecto no lo es.
- **Verificación.** `/migration-review` comprueba que cada pantalla migrada conserva las capacidades de su equivalente legacy.

## Restricciones
- No inventar reglas de negocio.
- No modificar producción ni utilizar credenciales productivas.
- No ejecutar operaciones destructivas sin autorización.
- No trasladar reglas críticas exclusivamente al frontend.
- No cambiar contratos, datos o dependencias fuera del alcance aprobado.
- No sustituir iBATIS de forma masiva durante el piloto.
- No declarar pruebas superadas si no se han ejecutado.
- No eliminar capacidades de la interfaz legacy sin aprobación (los widgets sí pueden sustituirse).

## Método
Inspeccionar → especificar → aclarar → planificar → descomponer → implementar → probar → revisar.

Skills: `/legacy-discovery`, `/business-rule-mining`, `/domain-ontology`, `/speckit-specify`, `/speckit-clarify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-analyze`, `/speckit-implement`, `/migration-review`.

## Finalización
Resume los cambios, pruebas ejecutadas, resultados, desviaciones, riesgos y decisiones pendientes.
