package es.medoncalls.guardias.vacaciones.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.Infraccion;
import es.medoncalls.guardias.cambios.domain.Mensajes;
import es.medoncalls.guardias.cambios.domain.ReglaCodigo;

/**
 * Reglas al modificar vacaciones (002 data-model, paso 5). Devuelve todas las que fallan, por fecha
 * (FR-011). No hay límite hacia el futuro (VAC-004).
 */
public class ReglasVacaciones {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Mensajes mensajes;

    public ReglasVacaciones(Mensajes mensajes) {
        this.mensajes = mensajes;
    }

    /**
     * @param guardias guardias del médico en el servicio del administrador, por fecha: las de otros
     *     servicios no cuentan (VAC-003, FR-010)
     */
    public List<Infraccion> evaluar(ModificacionVacaciones modificacion, LocalDate hoy, long medicoId, String nombre,
            Map<LocalDate, Guardia> guardias) {
        List<Infraccion> infracciones = new ArrayList<>();
        for (LocalDate dia : modificacion.anadir()) {
            if (dia.isBefore(hoy)) {
                infracciones.add(infraccion(ReglaCodigo.VAC_008, medicoId, nombre, dia, ""));
            }
            Guardia guardia = guardias.get(dia);
            if (guardia != null) {
                infracciones.add(infraccion(ReglaCodigo.VAC_003, medicoId, nombre, dia,
                        guardia.tipo().name().toLowerCase(Locale.ROOT)));
            }
        }
        return infracciones;
    }

    /** Días de `anadir` que aún no eran de vacaciones (FR-007). */
    public static List<LocalDate> anadidos(ModificacionVacaciones modificacion, Set<LocalDate> existentes) {
        return modificacion.anadir().stream().filter(d -> !existentes.contains(d)).toList();
    }

    private Infraccion infraccion(ReglaCodigo regla, long medicoId, String nombre, LocalDate dia, String extra) {
        return new Infraccion(regla, mensajes.texto("regla." + regla.codigo(), nombre, FECHA.format(dia), extra),
                medicoId, dia);
    }
}
