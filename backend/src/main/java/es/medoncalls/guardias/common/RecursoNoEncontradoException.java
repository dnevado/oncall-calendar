package es.medoncalls.guardias.common;

/** Guardia o médico inexistente en el servicio (404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
