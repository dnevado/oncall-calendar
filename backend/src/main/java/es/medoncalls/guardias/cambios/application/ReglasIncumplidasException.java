package es.medoncalls.guardias.cambios.application;

import java.util.List;

import es.medoncalls.guardias.cambios.domain.Infraccion;

/**
 * La operación incumple una o más reglas; no se ha modificado nada (422, FR-016). El texto es el
 * `detail` de la respuesta: por defecto, el del registro de cambios (001).
 */
public class ReglasIncumplidasException extends RuntimeException {

    public static final String TEXTO_CAMBIO = "El cambio no se ha registrado porque incumple las reglas indicadas.";

    private final transient List<Infraccion> infracciones;

    public ReglasIncumplidasException(List<Infraccion> infracciones) {
        this(TEXTO_CAMBIO, infracciones);
    }

    public ReglasIncumplidasException(String texto, List<Infraccion> infracciones) {
        super(texto);
        this.infracciones = List.copyOf(infracciones);
    }

    public List<Infraccion> infracciones() {
        return infracciones;
    }
}
