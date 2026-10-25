# Data Model: Gestión de vacaciones de los médicos

Sin cambios de esquema (research V4). Las tablas son las del legacy, compartidas durante la convivencia.

## Entidades

### Día de vacaciones

| Campo | Tabla / columna legacy | Notas |
|---|---|---|
| médico | `vacaciones_medicos.IdMedico` (int, NOT NULL) | Sin clave ajena en el esquema; la aplicación solo escribe médicos del servicio |
| fecha | `vacaciones_medicos.FDay` (varchar(10), NOT NULL) | Texto `yyyy-MM-dd` (`FechaLegacy`); el legacy compara como texto |
| — | `UNIQUE KEY IND_VACACIONES_01 (IdMedico, FDay)` | Un registro por médico y fecha (VAC-005) |

- Pertenece al **médico**, no al servicio (VAC-006): no hay columna de servicio y un día bloquea en todos sus servicios (CG-012).
- Sin estado ni auditoría (VAC-002; spec, «Sin historial»).
- **Conflicto** (derivado, no se guarda): el día coincide con una fila de `guardias_medicos` del mismo médico, con `LEFT(FGuardia, 10) = FDay` y `IdServicio` = servicio consultado (research V5).

### Médico del servicio

| Campo | Origen |
|---|---|
| id, nombre, apellidos | `medicos.ID`, `Nombre`, `Apellidos` |
| pertenencia | `guardias_servicios_medicos (IdServicio, IdMedico)` |
| activo en el servicio | `guardias_servicios_medicos.ActivoServicio` (como el listado legacy, `lista_medicos.jsp:121`) |
| administrador | `guardias_servicios_medicos.IsAdministrator` (comprobado por `ServicioAccess`) |

Se gestionan todos los médicos dados de alta en el servicio, activos o inactivos (spec, Assumptions).

### Guardia (solo lectura)

`guardias_medicos` (`Id`, `IdMedico`, `FGuardia`, `Tipo`, `Festivo`, `IdServicio`), igual que en la [001](../001-registro-cambios-guardia/data-model.md). Se usa para VAC-003 y para mostrar conflictos.

## Operación «modificar vacaciones»

Entrada: servicio, médico, `anadir` (fechas), `quitar` (fechas). Sin estados ni transiciones.

1. Comprobar que el usuario administra el servicio (403). (FR-001)
2. Validar la forma: alguna fecha; ninguna repetida en una lista ni presente en las dos; como mucho 366 por lista (400).
3. Abrir la transacción y bloquear el servicio (`lockServicio`, research V3) como **primera sentencia**: MySQL fija la vista de lectura en la primera consulta no bloqueante, así que las lecturas siguientes ven lo ya confirmado.
4. Comprobar que el médico pertenece al servicio (404). (FR-001)
5. Reglas, recogiendo **todas** las infracciones (FR-011):
   - **VAC-008:** toda fecha de `anadir` o `quitar` anterior a hoy es una infracción (FR-008).
   - **VAC-003:** toda fecha de `anadir` en la que el médico tiene guardia en el servicio es una infracción, con la guardia (FR-010).
   - Si hay infracciones: 422 y no se modifica nada (FR-006).
6. Calcular las diferencias frente a los días existentes de esas fechas:
   - `anadidos` = `anadir` − existentes, insertados con `ON DUPLICATE KEY UPDATE`;
   - `quitados` = `quitar` ∩ existentes, borrados por clave. (FR-005, FR-007)
7. Confirmar la transacción y devolver `anadidos` y `quitados`.

## Consulta

`listarVacaciones(servicio, desde, hasta, medicoId?)` devuelve los médicos del servicio (todos o uno) con sus días en `[desde, hasta]`, cada día con su conflicto si lo hay. Se ordena por apellidos y nombre, y los días por fecha. Rango como mucho de 731 días.

## Correspondencia con las reglas

| Regla | Dónde se cumple |
|---|---|
| VAC-001 | `ServicioAccess.exigirAdministrador` en ambas operaciones (servidor) |
| VAC-002 | Se escribe en la tabla en el acto; sin estados |
| VAC-003 | Paso 5, solo en el servicio del administrador |
| VAC-004 | No hay comprobación de límite futuro |
| VAC-005 | Clave única e inserción idempotente |
| VAC-006 | Sin servicio en la tabla; CG-012 de la 001 lee la misma tabla |
| VAC-007 | Operación por diferencias, en una transacción |
| VAC-008 | Paso 5 |
| VAC-009 | Consulta solo para administradores; conflictos derivados (V5) |
| VAC-010 / 011 | Misma tabla y formato; regresión con la SQL del legacy (quickstart, escenario 6) |
| VAC-012 | Sin notificaciones |
