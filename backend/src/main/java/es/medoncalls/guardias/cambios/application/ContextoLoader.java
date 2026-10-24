package es.medoncalls.guardias.cambios.application;

import java.time.Clock;
import java.time.LocalDate;
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
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.cambios.infrastructure.MedicoFila;
import es.medoncalls.guardias.cambios.infrastructure.MedicoMapper;
import es.medoncalls.guardias.cambios.infrastructure.VacacionesMapper;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;

/** Carga la foto de datos que evalúan las reglas (T035). */
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
        return new ContextoValidacion(solicitud, hoy(), origen, destino,
                participante(solicitud.cedenteId(), solicitud.servicioId(), fechas),
                participante(solicitud.receptorId(), solicitud.servicioId(), fechas));
    }

    public Participante participante(long medicoId, long servicioId, Set<LocalDate> fechas) {
        MedicoFila medico = medicos.buscar(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el médico " + medicoId + "."));
        Map<LocalDate, TipoMedico> tipos = new HashMap<>();
        Map<LocalDate, List<Guardia>> guardiasPorDia = new HashMap<>();
        Set<LocalDate> diasDeVacaciones = new HashSet<>();
        for (LocalDate fecha : fechas) {
            medicos.tipoEnFecha(medicoId, fecha).ifPresent(t -> tipos.put(fecha, t));
            guardiasPorDia.put(fecha, guardias.delMedicoEnFecha(medicoId, fecha, servicioId));
            if (vacaciones.tieneVacaciones(medicoId, fecha)) {
                diasDeVacaciones.add(fecha);
            }
        }
        return new Participante(medicoId, medico.nombreCompleto(), medico.estaActivo(),
                medicos.perteneceAServicio(medicoId, servicioId), tipos, guardiasPorDia, diasDeVacaciones);
    }
}
