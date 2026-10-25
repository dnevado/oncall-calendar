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

    // Vacaciones. `VacacionesDBImpl.getVacacionesMedicosFiltro` (`src/com/guardias/database/
    // VacacionesDBImpl.java:183-207`) concatena a la consulta base `WHERE IdMedico=<id>` y, según el
    // caso, `and  FDay='<día>'` o `and  FDay>='<desde>' and FDay<='<hasta>'`. Aquí los valores
    // concatenados son parámetros; los espacios y comillas del texto son los del legacy.

    /**
     * Vacaciones de un médico en un día: ficha del médico y `ProcesarMedicos.NoTieneVacaciones`
     * (`src/com/guardias/ProcesarMedicos.java:2505-2518`), que usan la generación y el relleno de la
     * agenda (`calc_agenda.jsp:525`, `fill_agenda.jsp:128`). Fuente: `VacacionesDBImpl.java:15`
     * (`SQLGENERAL`) y `getVacacionesMedicos`.
     */
    public static final String VACACIONES_DIA =
            "SELECT * FROM vacaciones_medicos" + " WHERE IdMedico=:idMedico" + " and  FDay=:dia";

    /** Vacaciones de un médico en un rango (selector de la ficha). Fuente: `VacacionesDBImpl.java:15`. */
    public static final String VACACIONES_RANGO =
            "SELECT * FROM vacaciones_medicos" + " WHERE IdMedico=:idMedico" + " and  FDay>=:desde and FDay<=:hasta";

    /**
     * Meses con vacaciones de un médico en un rango (columna «Vacaciones» del listado de médicos).
     * Fuente: `VacacionesDBImpl.java:17` (`SQLGENERAL_MESES`); el mes vuelve en la columna `fday`.
     */
    public static final String MESES_RANGO =
            "SELECT DISTINCT idmedico, EXTRACT(MONTH FROM date(fday)) fday  FROM vacaciones_medicos"
            + " WHERE IdMedico=:idMedico" + " and  FDay>=:desde and FDay<=:hasta";

    /**
     * Número de días de vacaciones de un médico en un rango (reparto de la agenda). Fuente:
     * `VacacionesDBImpl.java:19` (`SQLCOUNT`); el recuento vuelve en la columna `idmedico`.
     */
    public static final String RECUENTO_RANGO =
            "SELECT count(*) idmedico FROM vacaciones_medicos"
            + " WHERE IdMedico=:idMedico" + " and  FDay>=:desde and FDay<=:hasta";
}
