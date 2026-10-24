package es.medoncalls.guardias.cambios.infrastructure;

import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;

/** Fila de `guardias_medicos` con el nombre del médico asignado (null si no se pide). */
public record GuardiaFila(
        Long id,
        Long medicoId,
        String fecha,
        Integer festivo,
        String tipo,
        Long servicioId,
        String medicoNombre,
        String medicoApellidos) {

    public Guardia aGuardia() {
        return new Guardia(id, medicoId, FechaLegacy.fecha(fecha), TipoGuardia.desdeLegacy(tipo),
                festivo != null && festivo != 0, servicioId);
    }

    public String nombreCompleto() {
        return NombreMedico.de(medicoNombre, medicoApellidos);
    }
}
