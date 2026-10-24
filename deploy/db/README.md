# Limpieza de datos previa a las migraciones 004 y 005

Los changesets `004-guardias-cambios-pk` y `005-guardias-medicos-unicidad` **se detienen solos** (`onFail: HALT`) mientras existan duplicados. Para que puedan aplicarse hay que limpiar antes los datos de producción.

**Esta limpieza modifica datos de producción. NO se ejecuta sin:**

1. Una **copia de seguridad verificada** (restaurada con éxito en un entorno de pruebas).
2. La **revisión de los IDs concretos** que devuelven las consultas de diagnóstico de `limpieza-duplicados.sql`, junto con el responsable del proyecto.
3. La **confirmación explícita** del responsable antes de lanzar cualquier `UPDATE` o `DELETE` (constitución, flujo 6; plan 001, «Approvals» §2).

Casos esperados según el análisis del dump de 2026-08-26 (`docs/legacy/cambio-guardia-discovery.md` §8):

- 3 pares de `IdCambio` repetidos, todos con huellas de inserción manual (D3).
- 3 médicos con dos guardias el mismo día (D9). Cuál sobra es una **decisión de negocio** para cada caso.

El procedimiento completo de puesta en marcha está en `deploy/RUNBOOK.md` (T065).
