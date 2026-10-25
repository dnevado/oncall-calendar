# Quickstart: validación de la gestión de vacaciones

Guía para comprobar la funcionalidad de extremo a extremo. Contrato: [contracts/guardias-cambios.openapi.yaml](./contracts/guardias-cambios.openapi.yaml). Modelo y reglas: [data-model.md](./data-model.md).

## Requisitos previos

- Docker en marcha (Testcontainers con MySQL 5.7).
- Entorno local de la 001 (`deploy/local/README.md`): backend en 8081 y Vite en 5173, o el despliegue con nginx.
- Datos de prueba ficticios (`DatosPrueba`): un servicio con un administrador, un médico sin rol de administrador, dos médicos del servicio (uno inactivo) y un médico de otro servicio. Reloj fijo con `RelojPrueba`. Los ejemplos de esta tabla usan hoy = 2026-10-25; las pruebas automáticas usan fechas relativas a `IntegracionBase.HOY` (2030-01-15).

## Pruebas automáticas

```bash
mvn -f backend/pom.xml verify          # unitarias, integración, contrato y regresión legacy
npm --prefix frontend run gen:api      # tras publicar el contrato 1.2.0
npm --prefix frontend test
npm --prefix frontend run lint
npm --prefix frontend run build
```

## Escenarios

| # | Escenario | Pasos | Resultado esperado | Spec |
|---|---|---|---|---|
| 1 | Añadir varios días | `PATCH …/medicos/{B}/vacaciones` con `anadir` = 13 fechas futuras sin guardia | 200; `anadidos` = 13; el resto de vacaciones de B no cambia | US1-1, FR-003, FR-005 |
| 2 | Conflicto con guardia | B tiene guardia en el servicio el 2027-08-10; `anadir` del 2027-08-03 al 2027-08-14 | 422 con una infracción `VAC-003` del 2027-08-10; ningún día añadido | US1-2, FR-006, FR-010 |
| 3 | Días pasados y hoy | `anadir` 2026-10-20 → 422 `VAC-008`; `anadir` 2026-10-25 → 200 | Hoy permitido | US1-3, FR-008 |
| 4 | Sin límite futuro e idempotencia | `anadir` 2029-10-25, dos veces | 200 las dos; la segunda con `anadidos` vacío; una sola fila | US1-4/5, FR-007, FR-009 |
| 5 | Quitar solo lo indicado | `quitar` dos días de un periodo | Solo esos dos desaparecen | US2-1, FR-005 |
| 6 | Mezcla con error | `quitar` un día válido y `anadir` uno con guardia | 422; no se quita ni se añade nada | US2-3, FR-006 |
| 7 | Quitar un día pasado | `quitar` 2026-10-01 | 422 `VAC-008`; el día sigue | US2-2 |
| 8 | Permisos | Médico sin rol: GET y PATCH → 403. Administrador con médico de otro servicio → 404 | Sin cambios | FR-001, FR-002, SC-005 |
| 9 | Resumen y conflictos | `GET …/vacaciones?desde=hoy&hasta=hoy+6 meses` con un conflicto en los datos | Todos los médicos del servicio (también el inactivo y los que no tienen días); el día en conflicto con su guardia | US3, FR-012 a FR-014, SC-007 |
| 10 | Concurrencia con una cesión | En paralelo: añadir vacaciones a B el día D y cederle a B una guardia del día D | Solo una de las dos operaciones tiene éxito; nunca quedan las dos | research V3 |
| 11 | CG-012 tras añadir | Añadir vacaciones a B el día D y después intentar cederle una guardia de D | La cesión se rechaza con `CG-012` | US1-6, FR-015 |
| 12 | Regresión legacy | Añadir y quitar días por la API y ejecutar la SQL original de `VacacionesDBImpl` (día, rango, recuento y meses) y la de `NoTieneVacaciones` | El legacy ve exactamente los días escritos por la API | FR-016, SC-006 |
| 13 | Interfaz | En `/app/servicios/{id}/vacaciones`, elegir un médico, marcar varios días con ratón y con teclado, desmarcar otros y guardar | Resumen «añadir N, quitar M»; tras guardar, el calendario refleja el resultado; los días pasados no se pueden editar; los conflictos y errores se anuncian sin depender del color | FR-018 a FR-020, SC-001 |

## Comprobación manual en el entorno local

1. Iniciar sesión en `/app/` con el administrador ficticio de la 001 (`t067@prueba.local`) sobre la copia local.
2. Abrir «Vacaciones», elegir un médico y añadir un periodo futuro. Comprobar en el legacy (Tomcat local apuntando a la misma base) que la ficha del médico muestra esos días al abrir su selector.
3. Navegar el resumen a julio y agosto de 2017, que es donde están los 5 conflictos de la copia, todos del servicio 1. Comprobar que aparecen señalados y que no se pueden editar por ser pasados (SC-007, FR-008).
