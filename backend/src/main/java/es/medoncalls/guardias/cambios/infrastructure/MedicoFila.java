package es.medoncalls.guardias.cambios.infrastructure;

/** Médico identificado por `medicos.ID`. */
public record MedicoFila(Long id, String nombre, String apellidos, Integer activo) {

    public boolean estaActivo() {
        return activo != null && activo == 1;
    }

    public String nombreCompleto() {
        return NombreMedico.de(nombre, apellidos);
    }
}
