package es.medoncalls.guardias.vacaciones.infrastructure;

import es.medoncalls.guardias.cambios.infrastructure.NombreMedico;

/** Médico dado de alta en el servicio, con su actividad en él (`ActivoServicio`, como el listado legacy). */
public record MedicoServicioFila(Long id, String nombre, String apellidos, Integer activoServicio) {

    public String nombreCompleto() {
        return NombreMedico.de(nombre, apellidos);
    }

    public boolean activo() {
        return activoServicio != null && activoServicio != 0;
    }
}
