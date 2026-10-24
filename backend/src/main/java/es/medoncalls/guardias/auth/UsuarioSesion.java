package es.medoncalls.guardias.auth;

import java.io.Serializable;

/** Usuario autenticado guardado en la sesión del servidor. */
public record UsuarioSesion(long medicoId, String nombre) implements Serializable {
}
