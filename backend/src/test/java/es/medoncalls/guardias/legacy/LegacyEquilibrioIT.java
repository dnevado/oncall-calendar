package es.medoncalls.guardias.legacy;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;
import es.medoncalls.guardias.support.MySqlLegacyContainer;
import liquibase.integration.spring.SpringLiquibase;

/**
 * Historia 4, escenario 3: el cálculo de equilibrio del legacy no cambia (SC-006, FR-023; T057).
 * Se ejecutan sus dos consultas literales ({@link LegacySql}) sobre un conjunto fijo de datos
 * ficticios con la forma que deja el legacy, antes y después de aplicar las migraciones.
 */
class LegacyEquilibrioIT extends IntegracionBase {

    private static final long SERVICIO = 1;
    private static final long ANA = 1;
    private static final long BRUNO = 2;
    private static final long CARLA = 3;

    @Test
    void lasConsultasDeEquilibrioDanLoMismoAntesYDespuesDeLasMigraciones() throws Exception {
        DataSource base = MySqlLegacyContainer.baseSinMigrar("legacy_equilibrio");
        cargarDatosComoLosDejaElLegacy(new JdbcTemplate(base));

        List<List<Map<String, Object>>> antes = calcularEquilibrio(base);
        migrar(base);
        List<List<Map<String, Object>>> despues = calcularEquilibrio(base);

        assertThat(despues).isEqualTo(antes);
        // El conjunto no es trivial: hechos de Ana, Carla y Bruno y recibidos de Carla.
        assertThat(antes.stream().mapToInt(List::size).sum()).isEqualTo(4);
    }

    @Test
    void unaCesionRegistradaPorLaApiCuentaParaElCedenteConElTipoYFestivoCedidos() throws Exception {
        LocalDate dia10 = LocalDate.of(2030, 2, 10);
        long servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Admin");
        long ana = datos.adjunto("Ana");
        long bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, ana);
        datos.miembro(servicio, bruno);
        long localizada = datos.guardia(ana, dia10, TipoGuardia.LOCALIZADA, false, servicio);
        // Otra guardia del receptor, de otro tipo: no debe influir.
        datos.guardia(bruno, dia10.plusDays(10), TipoGuardia.PRESENCIA, true, servicio);

        mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(login(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + localizada + ",\"cedenteId\":" + ana
                                + ",\"receptorId\":" + bruno + "}"))
                .andExpect(status().isCreated());

        NamedParameterJdbcTemplate legacy = new NamedParameterJdbcTemplate(jdbc);
        List<Map<String, Object>> hechos = legacy.queryForList(LegacySql.TOTAL_CAMBIOS_HECHOS, Map.of(
                "idMedicoSolicitante", ana, "fechaIniCambio", "2030-02-01", "fechaFinCambio", "2030-02-28",
                "tipoCambio", "CESION", "estado", "APROBADA"));
        assertThat(hechos).hasSize(1);
        assertThat(hechos.get(0)).containsEntry("TipoCambio", "localizada").containsEntry("UsuarioAprobacion", 0);
        assertThat(((Number) hechos.get(0).get("IdCambio")).intValue()).isEqualTo(1);

        // Comportamiento actual del legacy, fuera de alcance (FR-023): la cesión no se descuenta al receptor.
        assertThat(legacy.queryForList(LegacySql.TOTAL_CAMBIOS_RECIBIDOS, Map.of(
                "idMedicoDestino", bruno, "fechaIniCambio", "2030-02-01", "fechaFinCambio", "2030-02-28",
                "tipoCambio", "CESION", "estado", "APROBADA", "idServicio", servicio))).isEmpty();
    }

    /**
     * Ana, Bruno y Carla, adjuntos del servicio 1, en febrero de 2030. Los cambios se insertan con
     * `IdCambio` explícito, como el legacy antes de la clave primaria (R7).
     */
    private static void cargarDatosComoLosDejaElLegacy(JdbcTemplate sql) {
        sql.update("INSERT INTO guardias_servicios (IdServicio, Nombre, Descripcion, IdMedicoOwner, CodigoInterno)"
                + " VALUES (1, 'Servicio', 'Servicio', 1, 'S1')");
        for (long id : new long[] {ANA, BRUNO, CARLA}) {
            sql.update("INSERT INTO medicos (Nombre, Apellidos, IdMedico, MaxGuardias, Orden, Tipo, Activo, ID)"
                    + " VALUES (?, 'Prueba', ?, 5, 1, 'ADJUNTO', 1, ?)", "M" + id, id, id);
            sql.update("INSERT INTO guardias_servicios_medicos (IdServicio, IdMedico, IsAdministrator, ActivoServicio)"
                    + " VALUES (1, ?, 0, 1)", id);
        }
        // Calendario después de los cambios de abajo.
        Object[][] guardias = {
            {BRUNO, "2030-02-03", 0, "presencia"}, // CAMBIO 1: era de Ana
            {ANA, "2030-02-07", 1, "localizada"}, // CAMBIO 1: era de Bruno
            {CARLA, "2030-02-12", 1, "presencia"}, // CESION 2 de Ana, FechaFin = FechaIni
            {BRUNO, "2030-02-14", 0, "refuerzo"}, // CESION 3 de Carla, FechaFin = FechaIni
            {CARLA, "2030-02-20", 0, "localizada"}, // CESION 4 de Ana, sin FechaFin (no se cuenta)
            {ANA, "2030-02-25", 0, "presencia"}, // CAMBIO 5 pendiente: no cuenta
            {CARLA, "2030-02-26", 0, "refuerzo"},
            {BRUNO, "2030-02-27", 1, "presencia"}, // CAMBIO 6 con las guardias sin intercambiar:
            {CARLA, "2030-02-28", 0, "localizada"}, // así cuentan las dos consultas
        };
        for (Object[] g : guardias) {
            sql.update("INSERT INTO guardias_medicos (IdMedico, FGuardia, Festivo, Tipo, IdServicio) VALUES (?, ?, ?, ?, 1)",
                    g[0], g[1], g[2], g[3]);
        }
        Object[][] cambios = {
            {1, "CAMBIO", "2030-02-03", "2030-02-07", ANA, BRUNO, "APROBADA"},
            {2, "CESION", "2030-02-12", "2030-02-12", ANA, CARLA, "APROBADA"},
            {3, "CESION", "2030-02-14", "2030-02-14", CARLA, BRUNO, "APROBADA"},
            {4, "CESION", "2030-02-20", null, ANA, CARLA, "APROBADA"},
            {5, "CAMBIO", "2030-02-25", "2030-02-26", CARLA, ANA, "PENDIENTE"},
            {6, "CAMBIO", "2030-02-27", "2030-02-28", BRUNO, CARLA, "APROBADA"},
        };
        for (Object[] c : cambios) {
            sql.update("INSERT INTO guardias_cambios (IdCambio, TipoCambio, FechaIniCambio, FechaFinCambio,"
                    + " IdMedicoSolicitante, IdMedicoDestino, Estado, FechaCreacion, IdServicio)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, '2030-01-10', 1)", c);
        }
    }

    /** Las dos consultas, para cada médico y tipo de cambio, en el orden fijo de los parámetros. */
    private static List<List<Map<String, Object>>> calcularEquilibrio(DataSource base) {
        NamedParameterJdbcTemplate legacy = new NamedParameterJdbcTemplate(base);
        List<List<Map<String, Object>>> resultados = new ArrayList<>();
        for (long medico : new long[] {ANA, BRUNO, CARLA}) {
            for (String tipo : new String[] {"CAMBIO", "CESION"}) {
                resultados.add(ordenados(legacy.queryForList(LegacySql.TOTAL_CAMBIOS_HECHOS, Map.of(
                        "idMedicoSolicitante", medico, "fechaIniCambio", "2030-02-01",
                        "fechaFinCambio", "2030-02-28", "tipoCambio", tipo, "estado", "APROBADA"))));
                resultados.add(ordenados(legacy.queryForList(LegacySql.TOTAL_CAMBIOS_RECIBIDOS, Map.of(
                        "idMedicoDestino", medico, "fechaIniCambio", "2030-02-01",
                        "fechaFinCambio", "2030-02-28", "tipoCambio", tipo, "estado", "APROBADA",
                        "idServicio", SERVICIO))));
            }
        }
        return resultados;
    }

    private static List<Map<String, Object>> ordenados(List<Map<String, Object>> filas) {
        return filas.stream()
                .sorted((a, b) -> String.valueOf(a.get("TipoCambio") + "|" + a.get("UsuarioAprobacion"))
                        .compareTo(b.get("TipoCambio") + "|" + b.get("UsuarioAprobacion")))
                .toList();
    }

    private static void migrar(DataSource base) throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(base);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        liquibase.setResourceLoader(new DefaultResourceLoader());
        liquibase.afterPropertiesSet();
    }
}
