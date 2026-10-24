package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;

/** Asignación de un médico a un día en un servicio. */
public record Guardia(long id, long medicoId, LocalDate fecha, TipoGuardia tipo, boolean festivo, long servicioId) {

    public Guardia conMedico(long nuevoMedicoId) {
        return new Guardia(id, nuevoMedicoId, fecha, tipo, festivo, servicioId);
    }

    public boolean esPresencia() {
        return tipo == TipoGuardia.PRESENCIA;
    }
}
