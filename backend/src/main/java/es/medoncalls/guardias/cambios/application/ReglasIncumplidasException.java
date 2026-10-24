package es.medoncalls.guardias.cambios.application;

import java.util.List;

import es.medoncalls.guardias.cambios.domain.Infraccion;

/** El cambio incumple una o más reglas; no se ha modificado nada (422, FR-016). */
public class ReglasIncumplidasException extends RuntimeException {

    private final transient List<Infraccion> infracciones;

    public ReglasIncumplidasException(List<Infraccion> infracciones) {
        super("El cambio incumple " + infracciones.size() + " regla(s).");
        this.infracciones = List.copyOf(infracciones);
    }

    public List<Infraccion> infracciones() {
        return infracciones;
    }
}
