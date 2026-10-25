# Quickstart: validación de la funcionalidad 001

Guía para comprobar de principio a fin que la funcionalidad cumple la [spec](./spec.md). Las rutas y nombres de tareas concretos los fija `tasks.md`; aquí se describe qué se ejecuta y qué se espera.

## Prerrequisitos

- Docker (para Testcontainers y para `deploy/docker-compose.yml`).
- JDK 21 y Maven 3.9+; Node.js 20+.
- **Sin datos de producción**: las pruebas usan el DDL legacy (solo esquema) y datos ficticios. El dump de `bbdd/` no se usa en pruebas (constitución, principio VI).

## 1. Pruebas automáticas

| Ejecución | Qué valida | Resultado esperado |
|---|---|---|
| `mvn -f backend/pom.xml verify` | Reglas (unitarias), transacción y bloqueo sobre MySQL 5.7 (Testcontainers), contrato contra [OpenAPI](./contracts/guardias-cambios.openapi.yaml), regresión legacy | Todo en verde |
| `npm --prefix frontend test` | Pantallas de login, calendario y alta, con MSW según el contrato | Todo en verde |

Las pruebas de integración del backend DEBEN cubrir, como mínimo:

1. **Historias 1 y 2**: cada escenario de aceptación de la spec, incluido un caso por regla incumplida (SC-002) y uno con varias reglas incumplidas a la vez (SC-003, FR-016).
2. **Historia 3**: entrada y salida de un R3–R5 en un día, con el residente como solicitante y como receptor (FR-020).
3. **Historia 4**: la cesión guarda el tipo y el festivo de la guardia cedida (FR-023).
4. **Atomicidad (SC-004)**: un fallo forzado a mitad de la aplicación no deja ni el cambio ni modificaciones del calendario.
5. **Concurrencia (SC-004, SC-005)**: dos altas simultáneas sobre la misma guardia; exactamente una tiene éxito y no queda ningún médico con dos guardias el mismo día.
6. **Permisos (SC-007)**: un médico no administrador, y un administrador de otro servicio, reciben 403 llamando directamente a la API.
7. **Idempotencia**: dos POST con la misma `Idempotency-Key` aplican el cambio una sola vez.
8. **Regresión legacy (SC-006, SC-008)**:
   - la SQL de calendario del legacy (`GuardiasDBImpl.getGuardias`) ve el cambio inmediatamente;
   - las consultas de equilibrio del legacy (`CambiosGuardias.xml` `getTotalCambiosHechosPorTipoMedicoFecha` y `getTotalCambiosRecibidosPorTipoMedicoFecha`) dan el mismo resultado antes y después de aplicar las migraciones sobre el mismo conjunto de datos, y cuentan la cesión nueva para el cedente con su tipo y festivo (R10).
9. **Login (R2)**: credenciales legacy válidas → sesión y hash adaptativo guardado; si después cambia `medicos.Password`, el hash nuevo se descarta. Tras 5 intentos fallidos la cuenta queda bloqueada 15 minutos en la aplicación nueva, también con la contraseña correcta, y un correo inexistente recibe el mismo 401 (FR-029).

## 2. Prueba manual de extremo a extremo

1. `docker compose -f deploy/docker-compose.yml up` con MySQL 5.7 y datos ficticios de un servicio, dos adjuntos y residentes R2 y R4.
2. Abrir `http://localhost/app/` e iniciar sesión como administrador del servicio. **Calendario (FR-030, FR-031)**: pasar por las vistas Año, Mes, Semana y Día; navegar con anterior/siguiente y volver con «Hoy»; pulsar el número de un día para ir a su vista; en un día con muchas guardias, abrir «+N más». **Esperado**: en cada vista se ven las guardias del periodo, con color y leyenda por tipo, los festivos marcados y, dentro de cada día, el orden presencia, residente, localizada y refuerzo. Repetirlo en un ancho de móvil.
3. **Cesión válida**: elegir una guardia futura de un adjunto, «Ceder», elegir otro adjunto candidato y confirmar. **Esperado**: el calendario muestra al receptor ese día con el mismo tipo; el listado de cambios muestra la cesión. Menos de 1 minuto (SC-001).
4. **Cesión inválida**: repetir eligiendo un receptor con vacaciones ese día. **Esperado**: mensaje con la regla, el médico y la fecha; el calendario no cambia.
5. **Intercambio**: intercambiar dos guardias futuras de dos adjuntos. **Esperado**: cada uno aparece en el día del otro y el tipo de cada día se mantiene.
6. **Ajuste R3–R5**: ceder la guardia del R2 al R4 en un día con un adjunto de refuerzo. **Esperado**: el adjunto pasa a localizada.
7. **Corrección**: registrar la cesión inversa del paso 3. **Esperado**: el calendario vuelve al estado inicial y quedan dos cambios en el listado.
8. **Legacy**: abrir el calendario del legacy (`http://localhost/`) y comprobar que refleja los pasos 3 a 7 (SC-008). Con `ACTIVAR_CAMBIO_GUARDIAS = 'N'`, intentar arrastrar una guardia en el legacy. **Esperado**: «El sistema no lo permite» (R13).
