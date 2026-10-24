package es.medoncalls.guardias.common;

/** El usuario de la sesión no administra el servicio (403, CG-002). */
public class ProhibidoException extends RuntimeException {

    public ProhibidoException() {
        super("No administras este servicio.");
    }
}
