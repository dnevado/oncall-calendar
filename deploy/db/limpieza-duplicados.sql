-- =============================================================================
-- Kit de limpieza previo a los changesets 004 y 005 (R7, R8).
--
-- ESTE FICHERO SOLO CONTIENE CONSULTAS DE DIAGNÓSTICO.
-- Las correcciones están comentadas y con marcadores: NO se ejecutan sin
--   1) copia de seguridad verificada,
--   2) revisión de los IDs concretos con el responsable del proyecto,
--   3) confirmación explícita (deploy/db/README.md, plan 001 «Approvals» §2).
-- =============================================================================

-- 1. IdCambio duplicados en guardias_cambios (esperados: 3 pares, inserciones manuales).
SELECT c.*
  FROM guardias_cambios c
  JOIN (SELECT IdCambio FROM guardias_cambios GROUP BY IdCambio HAVING COUNT(*) > 1) d
    ON d.IdCambio = c.IdCambio
 ORDER BY c.IdCambio, c.FechaCreacion;

-- 2. Médicos con más de una guardia el mismo día y servicio (esperados: 3 casos).
SELECT g.*
  FROM guardias_medicos g
  JOIN (SELECT IdMedico, LEFT(FGuardia, 10) AS Dia, IdServicio
          FROM guardias_medicos
         GROUP BY IdMedico, LEFT(FGuardia, 10), IdServicio
        HAVING COUNT(*) > 1) d
    ON d.IdMedico = g.IdMedico AND d.Dia = LEFT(g.FGuardia, 10) AND d.IdServicio <=> g.IdServicio
 ORDER BY g.IdMedico, g.FGuardia, g.Id;

-- -----------------------------------------------------------------------------
-- PLANTILLAS DE CORRECCIÓN (comentadas). Sustituir los marcadores tras la revisión.
-- -----------------------------------------------------------------------------
-- 1. Renumerar la copia manual de un IdCambio duplicado (una por par):
--    Identificar la fila manual (UsuarioAprobacion IS NULL y FechaAprobacion IS NULL).
-- UPDATE guardias_cambios
--    SET IdCambio = (SELECT n FROM (SELECT MAX(IdCambio) + 1 AS n FROM guardias_cambios) m)
--  WHERE IdCambio = <ID_DUPLICADO>
--    AND UsuarioAprobacion IS NULL AND FechaAprobacion IS NULL
--  LIMIT 1;
--
-- 2. Eliminar la guardia sobrante de un médico en un día (decisión de negocio por caso):
-- DELETE FROM guardias_medicos WHERE Id = <ID_GUARDIA_SOBRANTE>;
