package es.medoncalls.guardias.legacy;

/**
 * Copias literales de la SQL del legacy, usadas solo en pruebas de regresión: comprueban que lo
 * que escribe el sistema nuevo lo sigue leyendo el legacy igual que antes (FR-026, SC-008).
 * Los valores que el legacy concatena o enlaza se sustituyen por parámetros con nombre
 * (`:nombre`) para `NamedParameterJdbcTemplate`; el resto del texto no se toca.
 */
public final class LegacySql {

    private LegacySql() {
    }

    /**
     * Guardias de un día en el calendario del legacy. Fuente:
     * `src/com/guardias/database/GuardiasDBImpl.java:562-589`, con los filtros de servicio y fecha
     * (`FGuardia='…'`) y sin los opcionales (médico, tipo, festivo, evento).
     */
    public static final String CALENDARIO =
            "SELECT *, CASE WHEN Tipo = 'localizada' THEN 'flocalizada' WHEN Tipo = 'refuerzo' THEN 'flocalizada' WHEN Tipo='' THEN 'guardiaresidente'  ELSE Tipo END as Sorted "
            + " FROM guardias_medicos WHERE 1=1 and IdServicio=:servicioId"
            + " and  FGuardia=:fecha"
            + " ORDER BY Sorted  Desc";

    /**
     * Cambios hechos por un médico, por tipo y festivo de la guardia. Fuente:
     * `src/com/guardias/persistence/CambiosGuardias.xml:9-25`.
     */
    public static final String TOTAL_CAMBIOS_HECHOS = """
            SELECT
            		count(*) as IdCambio, GUARDIAS.Tipo as TipoCambio, GUARDIAS.Festivo as UsuarioAprobacion
            FROM
            		guardias_cambios CAMBIOS, guardias_medicos GUARDIAS
            WHERE
            		CAMBIOS.IdMedicoSolicitante = :idMedicoSolicitante and
            		CAMBIOS.FechaIniCambio >= :fechaIniCambio and
            		CAMBIOS.FechaIniCambio <= :fechaFinCambio and
            		TipoCambio = :tipoCambio and
            		CAMBIOS.FechaFinCambio = GUARDIAS.fGuardia and
            		CAMBIOS.IdMedicoDestino = GUARDIAS.IdMedico
            		and CAMBIOS.Estado = :estado
            GROUP BY
            		 GUARDIAS.Tipo, GUARDIAS.Festivo
            """;

    /**
     * Cambios recibidos por un médico, por tipo y festivo de la guardia. Fuente:
     * `src/com/guardias/persistence/CambiosGuardias.xml:28-44`.
     */
    public static final String TOTAL_CAMBIOS_RECIBIDOS = """
            SELECT
            		count(*) as IdCambio, GUARDIAS.Tipo as TipoCambio, GUARDIAS.Festivo as UsuarioAprobacion
            FROM
            		guardias_cambios CAMBIOS, guardias_medicos GUARDIAS
            WHERE
            		CAMBIOS.FechaFinCambio >=:fechaIniCambio
            		and  CAMBIOS.FechaFinCambio <= :fechaFinCambio
            		and CAMBIOS.IdMedicoDestino = :idMedicoDestino
            		and TipoCambio =:tipoCambio
            		and CAMBIOS.FechaIniCambio = GUARDIAS.fGuardia
            		and CAMBIOS.IdMedicoSolicitante = GUARDIAS.IdMedico
            		and CAMBIOS.Estado = :estado
            		and CAMBIOS.IdServicio = :idServicio
            		and GUARDIAS.IdServicio =CAMBIOS.IdServicio
            GROUP BY
            		 GUARDIAS.Tipo, GUARDIAS.Festivo
            """;
}
