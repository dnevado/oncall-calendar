package es.medoncalls.guardias.auth;

/**
 * Login rechazado. El mismo mensaje para cuenta inexistente, contraseña incorrecta, cuenta
 * inactiva o bloqueada, para no revelar si la cuenta existe (FR-029).
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos.");
    }
}
