package es.medoncalls.guardias.vacaciones.application;

import java.util.List;

import es.medoncalls.guardias.vacaciones.infrastructure.DiaVacacionFila;
import es.medoncalls.guardias.vacaciones.infrastructure.MedicoServicioFila;

/** Un médico del servicio con sus días de vacaciones del rango consultado, ordenados por fecha. */
public record VacacionesDelMedico(MedicoServicioFila medico, List<DiaVacacionFila> dias) {
}
