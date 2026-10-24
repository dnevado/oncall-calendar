# Data Model: Registro de cambios y cesiones de guardia

**Feature**: [spec.md](./spec.md) | **Research**: [research.md](./research.md)

El sistema nuevo usa **el mismo esquema MySQL que el legacy** durante la convivencia (constitución, restricciones técnicas). Cada entidad se describe con su significado de dominio y su correspondencia con el esquema. Los cambios de esquema son solo aditivos (R6); los marcados con 🔒 requieren autorización explícita porque modifican datos existentes (R7, R8).

## Entidades de dominio

### Servicio
- **Tabla**: `guardias_servicios` (lectura).
- **Campos**: `IdServicio`, `Nombre`, `Activo`.

### Médico
- **Tabla**: `medicos` (lectura; escritura solo de las columnas de autenticación nuevas).
- **Identificador**: `medicos.ID`. Es la columna con la que se relacionan las guardias (`guardias_medicos.IdMedico = medicos.ID`, `MedicoDBImpl.java:427`). **No** es `medicos.IdMedico`.
- **Campos usados**: `ID`, `Nombre`, `Apellidos`, `Email`, `Activo` (FR-014), `Confirmado` (login), `Tipo`, `SubTipo` (tipo actual; ver Tipo de médico).
- **Columnas nuevas** (nullable, ignoradas por el legacy):
  - `PasswordHashV2` `varchar(255)`: hash adaptativo (R2).
  - `PasswordHashV2Origen` `varchar(128)`: huella del `Password` legacy del que se derivó; si no coincide con el actual, se descarta el hash nuevo (R2).

### Pertenencia a servicio
- **Tabla**: `guardias_servicios_medicos` (lectura).
- **Campos**: `IdServicio`, `IdMedico` (= `medicos.ID`), `IsAdministrator` (rol de administrador del servicio, FR-001), `ActivoServicio` (pertenencia activa, FR-014).

### Tipo de médico (histórico) — nueva
- **Tabla nueva**: `medicos_tipo_historico`.

| Campo | Tipo | Notas |
|---|---|---|
| `Id` | int, PK, AUTO_INCREMENT | |
| `IdMedico` | int, not null | = `medicos.ID` |
| `Tipo` | varchar(20), not null | `ADJUNTO` / `RESIDENTE` |
| `SubTipo` | varchar(20), null | `R1`–`R5`, `SIMULADO`, `ROTANTE`; se ignora en adjuntos (D13) |
| `Desde` | date, null | `NULL` = «desde siempre» (carga inicial) |
| `Hasta` | date, null | `NULL` = vigente |

- **Índice**: `(IdMedico, Desde)`.
- **Alimentación**: carga inicial con el tipo actual de cada médico, más un trigger `AFTER UPDATE` en `medicos` que cierra el registro vigente y abre otro cuando cambian `Tipo` o `SubTipo` (R11).
- **Consulta**: el tipo en la fecha F es el registro con `(Desde IS NULL OR Desde <= F) AND (Hasta IS NULL OR Hasta > F)`.
- **Invariante**: un solo registro vigente por médico y fecha.

### Vacaciones
- **Tabla**: `vacaciones_medicos` (lectura).
- **Campos**: `IdMedico` (= `medicos.ID`) y `FDay` (`yyyy-MM-dd`); únicos por médico y día. Sin servicio: comunes a todos (FR-011).

### Guardia
- **Tabla**: `guardias_medicos` (lectura y escritura).

| Campo | Significado |
|---|---|
| `Id` | PK |
| `IdMedico` | médico asignado (= `medicos.ID`) |
| `FGuardia` | fecha `yyyy-MM-dd` (varchar) |
| `Tipo` | `presencia`, `localizada`, `refuerzo` o `''` (guardia de residente). Siempre en minúsculas |
| `Festivo` | 0/1 |
| `IdEventoGCalendar` | legacy; el sistema nuevo escribe `NULL` (CG-023 fuera de alcance) |
| `IdServicio` | servicio |

- **Cambio de esquema 🔒**: índice único `UX_GUARDIA_MEDICO_DIA (IdMedico, FGuardia(10), IdServicio)` (FR-025, R8), tras resolver los 3 duplicados existentes.
- **Invariantes**: un médico tiene como máximo una guardia por día y servicio (FR-010, FR-025).

### Cambio de guardia
- **Tabla**: `guardias_cambios` (escritura: alta; lectura: listado).
- **Correspondencia de cada alta del sistema nuevo:**

| Columna legacy | CAMBIO | CESION |
|---|---|---|
| `IdCambio` | asignado por la base (🔒 PK + AUTO_INCREMENT, R7) | igual |
| `TipoCambio` | `CAMBIO` | `CESION` |
| `FechaIniCambio` | fecha de la guardia del cedente | fecha de la guardia cedida |
| `FechaFinCambio` | fecha de la guardia del receptor | **= `FechaIniCambio`** (R10) |
| `IdMedicoSolicitante` | cedente | cedente |
| `IdMedicoDestino` | receptor | receptor |
| `Estado` | `APROBADA` (único valor; el legacy lo necesita para sus consultas) | igual |
| `FechaCreacion` / `FechaAprobacion` | fecha del registro (`yyyy-MM-dd`) | igual |
| `UsuarioAprobacion` | administrador que registra | igual |
| `IdServicio` | servicio | igual |
| `Observaciones` | `NULL` | `NULL` |

- **Columnas nuevas** (nullable; el legacy las ignora, MyBatis sin automapeo de columnas desconocidas):

| Columna | Tipo | CAMBIO | CESION |
|---|---|---|---|
| `TipoGuardiaOrigen` | varchar(20) | tipo de la guardia del cedente antes del cambio | tipo de la guardia cedida |
| `FestivoOrigen` | int | festivo de esa guardia | festivo de la guardia cedida |
| `TipoGuardiaDestino` | varchar(20) | tipo de la guardia del receptor antes del cambio | `NULL` |
| `FestivoDestino` | int | festivo de esa guardia | `NULL` |
| `FechaHoraRegistro` | datetime | instante exacto del registro | igual |
| `Canal` | varchar(20) | `APP` | `APP` |
| `IdempotencyKey` | varchar(64), único | clave de idempotencia, si llega | igual |

- `Canal` distingue los registros del sistema nuevo de los del legacy y de las inserciones manuales (discovery §8).
- **Inmutabilidad**: el sistema nuevo nunca actualiza ni borra filas de `guardias_cambios`. Una corrección es un cambio inverso nuevo (FR-028).

### Configuración
- **Tabla**: `configuracion`. El sistema nuevo **no lee** `ACTIVAR_CAMBIO_GUARDIAS` (CG-001). La puesta en marcha fija `'N'` para desactivar la pantalla del legacy (R13).

## Reglas de validación (servidor)

Se evalúan en una transacción, después de bloquear las guardias afectadas (R9). El sistema devuelve **todas** las que fallen (FR-016).

| Código | Regla | FR | Datos |
|---|---|---|---|
| `CG-002` | El usuario es administrador del servicio | FR-001 | `guardias_servicios_medicos.IsAdministrator` |
| `CG-005` | La guardia origen pertenece al cedente indicado | FR-005 | `guardias_medicos` |
| `CG-006` | CAMBIO: el receptor tiene la guardia destino | FR-006 | `guardias_medicos` |
| `CG-008` | Fecha origen > hoy | FR-008 | reloj del servidor (zona horaria del servicio) |
| `CG-009` | CAMBIO: fecha destino > hoy | FR-009 | idem |
| `CG-011` | Nadie con dos guardias el mismo día tras el cambio | FR-010 | `guardias_medicos` |
| `CG-012` | Ningún receptor de vacaciones en el día que recibe | FR-011 | `vacaciones_medicos` |
| `CG-013` | Sin presencias en días consecutivos (excepto el día adyacente que es la otra fecha del cambio; sin exención de simulados) | FR-012 | `guardias_medicos` con `Tipo = 'presencia'` |
| `CG-014` | Mismo tipo de médico en la fecha de cada guardia | FR-013 | `medicos_tipo_historico` |
| `CG-015` | Receptor activo y miembro activo del servicio | FR-014 | `medicos.Activo`, `guardias_servicios_medicos.ActivoServicio` |
| `MISMO_MEDICO` | Cedente ≠ receptor | FR-015 | — |

## Efectos en el calendario (misma transacción)

1. **CAMBIO** (FR-018): `guardiaOrigen.IdMedico ← receptor`, `guardiaDestino.IdMedico ← cedente`. `Tipo` y `Festivo` no cambian, porque pertenecen al día.
2. **CESION** (FR-019): `guardiaOrigen.IdMedico ← receptor`, sin más cambios. Se actualiza la fila existente en lugar de borrar e insertar como el legacy; el resultado visible es el mismo y se conserva el `Id`.
3. **Ajuste R3–R5** (FR-020), para cada fecha afectada: si el número de residentes R3–R5 (tipo en esa fecha) pasa de 0 a más de 0, las guardias de ese día y servicio con `Tipo = 'refuerzo'` pasan a `localizada`; si pasa de más de 0 a 0, las de `localizada` pasan a `refuerzo`. Es la misma acción que el legacy (`cambio_estado_guardia.jsp:206-226`), pero evaluada por día y sin depender de quién solicita.
4. **Registro**: inserción en `guardias_cambios` según la correspondencia anterior.

## Estados

No hay transiciones: un cambio existe solo si se aplicó (FR-002). `Estado = 'APROBADA'` es un valor fijo para que el legacy lo siga leyendo.
