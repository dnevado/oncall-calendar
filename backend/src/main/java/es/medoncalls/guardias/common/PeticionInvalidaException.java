package es.medoncalls.guardias.common;

/** Petición bien formada en JSON pero incoherente con el contrato (400). */
public class PeticionInvalidaException extends RuntimeException {

    public PeticionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
