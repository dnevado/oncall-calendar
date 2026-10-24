package es.medoncalls.guardias.cambios.application;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion;
import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;
import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.SolicitudCambio;
import es.medoncalls.guardias.cambios.domain.TipoMedico;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.cambios.infrastructure.HistoricoTipoFila;
import es.medoncalls.guardias.cambios.infrastructure.MedicoFila;
import es.medoncalls.guardias.cambios.infrastructure.MedicoMapper;
import es.medoncalls.guardias.cambios.infrastructure.VacacionFila;
import es.medoncalls.guardias.cambios.infrastructure.VacacionesMapper;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;

/**
 * Carga la foto de datos que evalúan las reglas (T035). Siempre por lotes: el mismo número de
 * consultas para un médico que para toda la plantilla de un servicio.
 */
@Component
public class ContextoLoader {

    private final GuardiaMapper guardias;
    private final MedicoMapper medicos;
    private final VacacionesMapper vacaciones;
    private final Clock clock;

    public ContextoLoader(GuardiaMapper guardias, MedicoMapper medicos, VacacionesMapper vacaciones, Clock clock) {
        this.guardias = guardias;
        this.medicos = medicos;
        this.vacaciones = vacaciones;
        this.clock = clock;
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    public ContextoValidacion cargar(SolicitudCambio solicitud, Guardia origen, Guardia destino) {
        Set<LocalDate> fechas = ContextoValidacion.fechasRelevantes(origen.fecha(),
                destino == null ? null : destino.fecha());
        Map<Long, Participante> participantes = participantes(
                List.of(solicitud.cedenteId(), solicitud.receptorId()), solicitud.servicioId(), fechas);
        return new ContextoValidacion(solicitud, hoy(), origen, destino,
                existente(participantes, solicitud.cedenteId()), existente(participantes, solicitud.receptorId()));
    }

    public Participante participante(long medicoId, long servicioId, Set<LocalDate> fechas) {
        return existente(participantes(List.of(medicoId), servicioId, fechas), medicoId);
    }

    /** Participantes por id de médico; los ids que no existen no aparecen. */
    public Map<Long, Participante> participantes(Collection<Long> ids, long servicioId, Set<LocalDate> fechas) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> miembros = new HashSet<>(medicos.selectMiembros(servicioId, ids));
        Map<Long, List<HistoricoTipoFila>> historicos = historicos(ids);
        Map<Long, List<Guardia>> guardiasDe = guardias.deMedicosEnFechas(ids, fechas, servicioId).stream()
                .collect(groupingBy(Guardia::medicoId));
        Map<Long, Set<LocalDate>> vacacionesDe = vacaciones
                .selectDias(ids, fechas.stream().map(FechaLegacy::texto).toList()).stream()
                .collect(groupingBy(VacacionFila::medicoId, mapping(v -> FechaLegacy.fecha(v.dia()), toSet())));

        Map<Long, Participante> participantes = new HashMap<>();
        for (MedicoFila medico : medicos.selectByIds(ids)) {
            long id = medico.id();
            List<HistoricoTipoFila> historico = historicos.getOrDefault(id, List.of());
            List<Guardia> suyas = guardiasDe.getOrDefault(id, List.of());
            Map<LocalDate, TipoMedico> tipos = new HashMap<>();
            Map<LocalDate, List<Guardia>> guardiasPorDia = new HashMap<>();
            for (LocalDate fecha : fechas) {
                HistoricoTipoFila.tipoEn(historico, fecha).ifPresent(t -> tipos.put(fecha, t));
                guardiasPorDia.put(fecha, suyas.stream().filter(g -> g.fecha().equals(fecha)).toList());
            }
            participantes.put(id, new Participante(id, medico.nombreCompleto(), medico.estaActivo(),
                    miembros.contains(id), tipos, guardiasPorDia, vacacionesDe.getOrDefault(id, Set.of())));
        }
        return participantes;
    }

    /** Histórico de tipo de varios médicos, para el ajuste R3–R5 (FR-020). */
    public Map<Long, List<HistoricoTipoFila>> historicos(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return medicos.selectHistoricoTipo(ids).stream().collect(groupingBy(HistoricoTipoFila::medicoId));
    }

    private static Participante existente(Map<Long, Participante> participantes, long medicoId) {
        Participante participante = participantes.get(medicoId);
        if (participante == null) {
            throw new RecursoNoEncontradoException("No existe el médico " + medicoId + ".");
        }
        return participante;
    }
}
