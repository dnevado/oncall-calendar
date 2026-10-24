package es.medoncalls.guardias.auth;

import java.time.LocalDateTime;

/** Datos de `medicos` necesarios para el login. */
public record MedicoAuthFila(
        Long id,
        String nombre,
        String apellidos,
        String password,
        String passwordHashV2,
        String passwordHashV2Origen,
        Integer loginFallidosV2,
        LocalDateTime bloqueadoHastaV2,
        Integer activo,
        Integer confirmado) {

    public boolean puedeEntrar() {
        return activo != null && activo == 1 && confirmado != null && confirmado == 1;
    }

    public boolean bloqueadoEn(LocalDateTime ahora) {
        return bloqueadoHastaV2 != null && ahora.isBefore(bloqueadoHastaV2);
    }
}
