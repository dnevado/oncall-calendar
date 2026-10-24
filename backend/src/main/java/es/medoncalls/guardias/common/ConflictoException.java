package es.medoncalls.guardias.common;

/** La Idempotency-Key ya se usó con una petición distinta (409). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
