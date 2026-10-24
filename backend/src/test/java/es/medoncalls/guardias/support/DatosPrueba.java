package es.medoncalls.guardias.support;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import es.medoncalls.guardias.auth.LegacyPasswordVerifier;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;

/**
 * Constructores de datos ficticios sobre el esquema legacy (principio VI: ningún dato real).
 * Los médicos se identifican por `medicos.ID`; el trigger de Liquibase crea su histórico de tipo.
 */
public class DatosPrueba {

    public static final String PASSWORD = "secreto-de-prueba";

    private static final String[] TABLAS = {"guardias_cambios", "guardias_medicos", "vacaciones_medicos",
        "guardias_servicios_medicos", "guardias_servicios", "medicos_tipo_historico", "medicos", "configuracion"};

    private final JdbcTemplate jdbc;
    private final AtomicInteger secuencia = new AtomicInteger(100);

    public DatosPrueba(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void limpiar() {
        for (String tabla : TABLAS) {
            jdbc.execute("DELETE FROM " + tabla);
        }
    }

    public long servicio(String nombre) {
        long id = secuencia.incrementAndGet();
        jdbc.update("INSERT INTO guardias_servicios (IdServicio, Nombre, Descripcion, IdMedicoOwner, CodigoInterno)"
                + " VALUES (?, ?, ?, 0, ?)", id, nombre, nombre, "S" + id);
        return id;
    }

    public long adjunto(String nombre) {
        return medico(nombre, "ADJUNTO", null, true);
    }

    public long residente(String nombre, String subtipo) {
        return medico(nombre, "RESIDENTE", subtipo, true);
    }

    public long medico(String nombre, String tipo, String subtipo, boolean activo) {
        long id = secuencia.incrementAndGet();
        jdbc.update("INSERT INTO medicos (Nombre, Apellidos, IdMedico, MaxGuardias, Orden, Tipo, SubTipo, Activo, ID,"
                + " Email, Password, Confirmado) VALUES (?, ?, ?, 5, 1, ?, ?, ?, ?, ?, ?, 1)",
                nombre, "Prueba", id, tipo, subtipo, activo ? 1 : 0, id, email(id),
                new LegacyPasswordVerifier().hashLegacy(PASSWORD));
        return id;
    }

    public String email(long medicoId) {
        return "medico" + medicoId + "@ejemplo.test";
    }

    public void miembro(long servicioId, long medicoId, boolean administrador, boolean activoServicio) {
        jdbc.update("INSERT INTO guardias_servicios_medicos (IdServicio, IdMedico, IsAdministrator, ActivoServicio)"
                + " VALUES (?, ?, ?, ?)", servicioId, medicoId, administrador ? 1 : 0, activoServicio ? 1 : 0);
    }

    public void miembro(long servicioId, long medicoId) {
        miembro(servicioId, medicoId, false, true);
    }

    public long guardia(long medicoId, LocalDate fecha, TipoGuardia tipo, boolean festivo, long servicioId) {
        GeneratedKeyHolder clave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO guardias_medicos (IdMedico, FGuardia, Festivo, Tipo,"
                    + " IdServicio) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, medicoId);
            ps.setString(2, fecha.toString());
            ps.setInt(3, festivo ? 1 : 0);
            ps.setString(4, tipo.valorLegacy());
            ps.setLong(5, servicioId);
            return ps;
        }, clave);
        return clave.getKey().longValue();
    }

    public void vacaciones(long medicoId, LocalDate dia) {
        jdbc.update("INSERT INTO vacaciones_medicos (IdMedico, FDay) VALUES (?, ?)", medicoId, dia.toString());
    }

    public void configuracion(String clave, String valor, long servicioId) {
        jdbc.update("INSERT INTO configuracion (config_key, config_value, config_IdServicio) VALUES (?, ?, ?)",
                clave, valor, servicioId);
    }

    /** Cierra el tipo vigente en `desde` y abre el nuevo (lo que haría el trigger con otra fecha). */
    public void cambiarTipoDesde(long medicoId, String tipo, String subtipo, LocalDate desde) {
        jdbc.update("UPDATE medicos_tipo_historico SET Hasta = ? WHERE IdMedico = ? AND Hasta IS NULL", desde, medicoId);
        jdbc.update("INSERT INTO medicos_tipo_historico (IdMedico, Tipo, SubTipo, Desde, Hasta) VALUES (?, ?, ?, ?, NULL)",
                medicoId, tipo, subtipo, desde);
    }

    public Long medicoDeGuardia(long guardiaId) {
        return jdbc.queryForObject("SELECT IdMedico FROM guardias_medicos WHERE Id = ?", Long.class, guardiaId);
    }

    public int contar(String tabla) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
    }
}
