package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Foto de los datos que necesitan las reglas, tomada dentro de la transacción después de bloquear
 * las guardias afectadas (R9). Las reglas son funciones puras sobre esta foto.
 *
 * @param destino guardia del receptor en un CAMBIO; null en una CESION
 */
public record ContextoValidacion(
        SolicitudCambio solicitud,
        LocalDate hoy,
        Guardia origen,
        Guardia destino,
        Participante cedente,
        Participante receptor) {

    /**
     * Médico implicado, con sus guardias en el servicio, vacaciones y tipo en las fechas
     * relevantes (las del cambio y sus días adyacentes).
     */
    public record Participante(
            long id,
            String nombre,
            boolean activo,
            boolean miembroDelServicio,
            Map<LocalDate, TipoMedico> tipos,
            Map<LocalDate, List<Guardia>> guardias,
            Set<LocalDate> vacaciones) {

        public Optional<TipoMedico> tipoEn(LocalDate fecha) {
            return Optional.ofNullable(tipos.get(fecha));
        }

        public List<Guardia> guardiasEn(LocalDate fecha) {
            return guardias.getOrDefault(fecha, List.of());
        }

        public boolean deVacacionesEn(LocalDate fecha) {
            return vacaciones.contains(fecha);
        }
    }

    /** Fechas cuyos datos hay que cargar: las del cambio y sus días anterior y posterior. */
    public static Set<LocalDate> fechasRelevantes(LocalDate fechaOrigen, LocalDate fechaDestino) {
        var fechas = new TreeSet<LocalDate>();
        for (LocalDate f : fechaDestino == null ? List.of(fechaOrigen) : List.of(fechaOrigen, fechaDestino)) {
            fechas.add(f.minusDays(1));
            fechas.add(f);
            fechas.add(f.plusDays(1));
        }
        return fechas;
    }
}
