<!--
Sync Impact Report (1.2.0)
- Versión: 1.1.0 → 1.2.0 (MINOR: nuevo principio)
- Principios añadidos: VIII. Capacidades de la interfaz preservadas
- Secciones modificadas: Flujo de trabajo y puertas de calidad (punto 4: la revisión incluye
  las capacidades de la interfaz)
- Secciones eliminadas: ninguna
- Plantillas dependientes: no modificadas (leen la constitución en tiempo de ejecución)
- Aprobada el 2026-10-25 por el responsable del proyecto. Sin TODO pendientes.

Sync Impact Report (1.1.0)
- Versión: 1.0.0 → 1.1.0 (MINOR: se fija la línea base tecnológica y la ubicación de los
  contratos; los principios no cambian)
- Secciones modificadas: Restricciones técnicas (stack fijado; sin referencias a código
  previo); Principio IV (ubicación de contratos)
- TODO resueltos: FRONTEND_TYPESCRIPT (React + TypeScript según CLAUDE.md),
  BACKEND_BASELINE (Spring Boot 3.x + Java 21), OPENAPI_LOCATION (contracts/)
- Contexto: frontend-react/, backend-spring-lambda/ e infra/ eran un intento previo no
  publicado y quedan fuera de la línea base (origin/master solo contiene el legacy).
- Ratificada el 2026-10-24 por el responsable del proyecto, incluido el principio VII.
  Las carpetas del intento previo se han eliminado del disco. Sin TODO pendientes.

Sync Impact Report (1.0.0)
- Versión: plantilla sin versión → 1.0.0 (adopción inicial; propuesta pendiente de aprobación)
- Principios definidos (nuevos):
  I. Reglas de negocio validadas, nunca inventadas
  II. Reglas críticas y autorización en el servidor
  III. Migración incremental con paridad verificable
  IV. Contrato primero
  V. Evidencia trazable y pruebas ejecutadas
  VI. Protección de datos y secretos
  VII. Integridad de datos en el servidor
- Secciones añadidas: Restricciones técnicas; Flujo de trabajo y puertas de calidad; Gobierno
- Secciones eliminadas: ninguna
- Plantillas dependientes: no modificadas por este comando (leen la constitución en tiempo de ejecución)
- TODO diferidos: RATIFICATION_DATE, FRONTEND_TYPESCRIPT, BACKEND_BASELINE,
  OPENAPI_LOCATION (los tres últimos, resueltos en 1.1.0)
-->

# Constitución de Gestión de Guardias (medONCALLS)

## Core Principles

### I. Reglas de negocio validadas, nunca inventadas

- Toda regla funcional que implemente el sistema nuevo DEBE estar en el catálogo de reglas
  versionado y validado por negocio, o en una especificación aprobada de Spec Kit.
- El comportamiento legacy es **evidencia**, no requisito: reproducirlo, corregirlo o
  descartarlo es una decisión explícita, documentada en la especificación.
- Las dudas DEBEN registrarse como preguntas abiertas (`[NEEDS CLARIFICATION]` o cuestionario
  de negocio), nunca como suposiciones.

**Razón:** el legacy contiene validaciones contradictorias, comentadas o sin efecto (p. ej.
`VALIDAR_CAMBIOS_GUARDIAS_BY_ADM`); copiarlas sin validar perpetuaría errores.

### II. Reglas críticas y autorización en el servidor

- Toda regla que proteja la integridad del calendario, los permisos o los datos DEBE
  validarse en el backend. El frontend PUEDE repetirla solo como ayuda de usabilidad.
- La identidad y el rol se obtienen de la sesión o del token autenticado; el servidor NO DEBE
  confiar en identificadores de usuario, roles ni estados enviados por el cliente.
- Cada endpoint DEBE declarar y comprobar qué rol puede invocarlo, y repetir las validaciones
  de negocio aunque un paso previo ya las haya hecho.

**Razón:** en el legacy, aprobar cambios o actuar en nombre de otro médico solo está
restringido en la interfaz (`cambio_estado_guardia.jsp`, `add_cambio_guardia.jsp`).

### III. Migración incremental con paridad verificable

- La modernización se hace por incrementos acotados (pantalla, flujo o caso de uso); cada uno
  DEBE poder convivir con el legacy y desplegarse por separado.
- El código legacy es de solo lectura salvo cambio aprobado en la especificación.
  iBATIS NO DEBE sustituirse de forma masiva durante el piloto.
- Cada incremento DEBE documentar, por regla, si mantiene la paridad con el legacy o la cambia
  a propósito, y DEBE tener pruebas que lo demuestren.
- No se cambian contratos, datos ni dependencias fuera del alcance aprobado.

**Razón:** limita el riesgo en un sistema en uso diario sin pruebas automáticas previas.

### IV. Contrato primero

- Toda API entre el frontend React y el backend Spring Boot DEBE definirse en un contrato
  OpenAPI versionado antes de implementarse, en `contracts/<servicio>.openapi.yaml`.
  Los contratos de `specs/<feature>/contracts/` son borradores hasta que se publican allí.
- El código y las pruebas DEBEN ajustarse al contrato; un cambio incompatible exige nueva
  versión mayor del contrato y aprobación explícita.
- Las respuestas de error DEBEN ser estructuradas (código y mensaje), no texto libre del tipo
  `NOOK.<mensaje>`.

**Razón:** el legacy mezcla HTML, texto y JSON en el mismo endpoint, y el cliente interpreta
los errores buscando cadenas, lo que convierte fallos en falsos éxitos.

### V. Evidencia trazable y pruebas ejecutadas

- Toda afirmación sobre el comportamiento legacy DEBE citar su evidencia (`ruta:línea`,
  commit o consulta agregada).
- Ninguna prueba se declara superada si no se ha ejecutado; los informes DEBEN indicar qué se
  ejecutó, el resultado y lo que se omitió.
- Toda regla del catálogo implementada DEBE tener al menos una prueba automática en el
  servidor; las reglas críticas, además, prueba de integración contra base de datos.

**Razón:** las especificaciones se basan en evidencias verificables, no en recuerdos ni en
comentarios del código.

### VI. Protección de datos y secretos

- NO se usan credenciales ni entornos de producción. Los datos productivos solo se usan con
  autorización explícita del propietario, en solo lectura y fuera del control de versiones.
- Los documentos, pruebas y fixtures NO DEBEN contener datos personales reales (nombres,
  emails, vacaciones identificables); se usan agregados o datos ficticios.
- Los secretos (contraseñas, claves de servicio, tokens) NO DEBEN almacenarse en el código, en
  ficheros versionados ni en tablas de configuración en claro; el sistema nuevo DEBE usar un
  gestor de secretos.
- Las contraseñas de usuario DEBEN almacenarse con hash adaptativo.

**Razón:** el sistema gestiona datos de personal sanitario y el legacy guarda secretos en
claro en `configuracion` y `medicos`.

### VII. Integridad de datos en el servidor

- Las operaciones que modifican varias filas (p. ej. aplicar un cambio de guardia) DEBEN ser
  atómicas en una única transacción.
- Los identificadores DEBEN generarse en la base de datos o con un mecanismo seguro ante
  concurrencia; queda prohibido `max(id)+1`.
- Las consultas DEBEN usar parámetros enlazados; queda prohibido concatenar entradas del
  usuario en SQL.
- Las invariantes de datos acordadas DEBEN reforzarse con restricciones de base de datos
  (claves, unicidad) además de con validaciones de código.
- Las operaciones de alta DEBEN ser idempotentes o detectar duplicados.

**Razón:** el dump analizado contiene `IdCambio` duplicados, cambios repetidos y cambios
aprobados sin aplicar.

### VIII. Capacidades de la interfaz preservadas

- Cada pantalla migrada DEBE conservar las capacidades de su equivalente legacy; los widgets,
  la disposición y el aspecto PUEDEN sustituirse por equivalentes modernos en React.
- Eliminar una capacidad legacy es una decisión explícita, registrada en la especificación y
  aprobada.
- Cada plan DEBE listar las capacidades de la pantalla legacy y cómo las cubre la nueva;
  `/migration-review` DEBE verificarlo.

**Razón:** los usuarios trabajan a diario con el calendario legacy (vistas año/mes/semana/día,
mover guardias, selección de varias fechas); perder capacidades bloquea la adopción, pero el
aspecto y los widgets deben poder modernizarse.

## Restricciones técnicas

- **Legacy (referencia, solo lectura):** JSP + jQuery, Java, iBATIS/MyBatis y MySQL 5.7.
- **Línea base:** el código de `origin/master` (solo legacy). Los trabajos previos no
  publicados (`frontend-react/`, `backend-spring-lambda/`, `infra/`) NO son referencia.
- **Backend nuevo:** Spring Boot 3.x sobre Java 21. Las desviaciones de versión DEBEN
  justificarse en el plan.
- **Frontend nuevo:** React + TypeScript; no se admite código nuevo en JavaScript.
- **Infraestructura:** como código; la herramienta y el destino de despliegue se deciden en
  el primer `/speckit-plan`.
- **Contratos:** OpenAPI 3 versionado en `contracts/` (ver principio IV).
- **Ubicación del código nuevo:** se fija en el primer plan; no se reutilizan las carpetas
  del intento previo.
- **Datos:** el esquema MySQL legacy es la fuente de verdad durante la convivencia; cualquier
  cambio de esquema DEBE ser compatible hacia atrás con el legacy mientras ambos estén activos.
- Las nuevas dependencias DEBEN justificarse en el plan de la funcionalidad.

## Flujo de trabajo y puertas de calidad

1. **Método:** inspeccionar → especificar → aclarar → planificar → descomponer → implementar →
   probar → revisar (`/legacy-discovery`, `/business-rule-mining`, `/speckit-specify`,
   `/speckit-clarify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`,
   `/migration-review`).
2. **Antes de planificar:** la especificación NO DEBE tener preguntas abiertas que afecten a
   reglas críticas.
3. **Antes de implementar:** `/speckit-analyze` sin incoherencias críticas entre spec, plan y
   tareas, y comprobación de esta constitución en el plan.
4. **Antes de abrir un PR:** `/migration-review` frente a spec, catálogo de reglas, contrato,
   comportamiento legacy y capacidades de la interfaz legacy; pruebas ejecutadas y resultados
   adjuntos.
5. **Al terminar cada tarea:** resumen de cambios, pruebas ejecutadas, resultados, desviaciones,
   riesgos y decisiones pendientes.
6. No se ejecutan operaciones destructivas (borrados, migraciones de datos, force-push) sin
   autorización explícita.

## Governance

- Esta constitución prevalece sobre otras prácticas del proyecto. `CLAUDE.md` y las skills en
  `.claude/skills/` son guías operativas y NO DEBEN contradecirla.
- **Enmiendas:** se proponen por escrito con su motivo e impacto, las aprueba el responsable
  del proyecto y se registran en el Sync Impact Report de este fichero.
- **Versionado semántico:** MAJOR para eliminar o redefinir principios; MINOR para añadir
  principios o secciones, o ampliar guías de forma material; PATCH para aclaraciones.
- **Cumplimiento:** cada plan (`/speckit-plan`) y cada revisión (`/migration-review`) DEBE
  verificar el cumplimiento; toda excepción DEBE justificarse en el plan, con su alcance y su
  fecha de revisión.

**Version**: 1.2.0 | **Ratified**: 2026-10-24 | **Last Amended**: 2026-10-25
