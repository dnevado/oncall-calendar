package es.medoncalls.guardias.vacaciones.application;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.common.PeticionInvalidaException;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;
import es.medoncalls.guardias.vacaciones.infrastructure.DiaVacacionFila;
import es.medoncalls.guardias.vacaciones.infrastructure.VacacionMapper;

/** Vacaciones de los médicos del servicio en un rango, con los conflictos (002 FR-012 a FR-014). */
@Service
public class ConsultarVacacionesService {

    /** Límite técnico del rango, no regla de negocio (research V7). */
    static final long MAX_DIAS_RANGO = 731;

    private final VacacionMapper vacaciones;

    public ConsultarVacacionesService(VacacionMapper vacaciones) {
        this.vacaciones = vacaciones;
    }

    @Transactional(readOnly = true)
    public List<VacacionesDelMedico> consultar(long servicioId, LocalDate desde, LocalDate hasta, Long medicoId) {
        if (hasta.isBefore(desde)) {
            throw new PeticionInvalidaException("La fecha «hasta» es anterior a «desde».");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS_RANGO) {
            throw new PeticionInvalidaException("El rango no puede superar " + MAX_DIAS_RANGO + " días.");
        }
        if (medicoId != null && !vacaciones.perteneceAlServicio(servicioId, medicoId)) {
            throw new RecursoNoEncontradoException("El médico no pertenece al servicio.");
        }
        Map<Long, List<DiaVacacionFila>> dias = vacaciones
                .diasConConflicto(servicioId, FechaLegacy.texto(desde), FechaLegacy.texto(hasta), medicoId).stream()
                .collect(Collectors.groupingBy(DiaVacacionFila::medicoId));
        return vacaciones.medicosDelServicio(servicioId, medicoId).stream()
                .map(m -> new VacacionesDelMedico(m, dias.getOrDefault(m.id(), List.of())))
                .toList();
    }
}
