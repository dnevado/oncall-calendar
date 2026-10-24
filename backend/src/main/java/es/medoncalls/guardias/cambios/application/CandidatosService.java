package es.medoncalls.guardias.cambios.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion;
import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;
import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.ReglasCambio;
import es.medoncalls.guardias.cambios.domain.SolicitudCambio;
import es.medoncalls.guardias.cambios.domain.TipoCambio;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.cambios.infrastructure.MedicoFila;
import es.medoncalls.guardias.cambios.infrastructure.MedicoMapper;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;

/**
 * Receptores que el administrador puede elegir (FR-017). Es solo una ayuda de interfaz: se
 * ofrecen los médicos para los que las mismas reglas del registro no dan ninguna infracción; el
 * alta vuelve a validarlo todo.
 */
@Service
public class CandidatosService {

    /** Médico candidato y, en un CAMBIO, su guardia en la fecha destino. */
    public record Candidato(long medicoId, String nombre, Long guardiaDestinoId) {
    }

    private final GuardiaMapper guardias;
    private final MedicoMapper medicos;
    private final ContextoLoader contextos;
    private final ReglasCambio reglas;

    public CandidatosService(GuardiaMapper guardias, MedicoMapper medicos, ContextoLoader contextos,
            ReglasCambio reglas) {
        this.guardias = guardias;
        this.medicos = medicos;
        this.contextos = contextos;
        this.reglas = reglas;
    }

    /** Médicos activos del servicio que pueden recibir la guardia. */
    @Transactional(readOnly = true)
    public List<Candidato> paraCesion(long servicioId, long guardiaId) {
        Guardia origen = guardiaDelServicio(servicioId, guardiaId);
        Set<LocalDate> fechas = ContextoValidacion.fechasRelevantes(origen.fecha(), null);
        Participante cedente = contextos.participante(origen.medicoId(), servicioId, fechas);
        LocalDate hoy = contextos.hoy();

        List<MedicoFila> activos = medicos.selectActivosDelServicio(servicioId).stream()
                .filter(m -> m.id() != origen.medicoId())
                .toList();
        Map<Long, Participante> receptores = contextos.participantes(
                activos.stream().map(MedicoFila::id).toList(), servicioId, fechas);

        List<Candidato> candidatos = new ArrayList<>();
        for (MedicoFila medico : activos) {
            SolicitudCambio hipotetica = new SolicitudCambio(TipoCambio.CESION, servicioId, origen.id(),
                    origen.medicoId(), medico.id(), null);
            ContextoValidacion contexto = new ContextoValidacion(hipotetica, hoy, origen, null, cedente,
                    receptores.get(medico.id()));
            if (reglas.evaluar(contexto).isEmpty()) {
                candidatos.add(new Candidato(medico.id(), medico.nombreCompleto(), null));
            }
        }
        return candidatos;
    }

    /** Médicos con guardia en la fecha destino con los que se puede intercambiar la guardia. */
    @Transactional(readOnly = true)
    public List<Candidato> paraIntercambio(long servicioId, long guardiaId, LocalDate fechaDestino) {
        Guardia origen = guardiaDelServicio(servicioId, guardiaId);
        Set<LocalDate> fechas = ContextoValidacion.fechasRelevantes(origen.fecha(), fechaDestino);
        Participante cedente = contextos.participante(origen.medicoId(), servicioId, fechas);
        LocalDate hoy = contextos.hoy();

        List<Guardia> delDia = guardias.delServicioEnFecha(servicioId, fechaDestino).stream()
                .filter(g -> g.medicoId() != origen.medicoId())
                .toList();
        Map<Long, Participante> receptores = contextos.participantes(
                delDia.stream().map(Guardia::medicoId).distinct().toList(), servicioId, fechas);

        List<Candidato> candidatos = new ArrayList<>();
        for (Guardia destino : delDia) {
            Participante receptor = receptores.get(destino.medicoId());
            if (receptor == null) {
                continue;
            }
            SolicitudCambio hipotetica = new SolicitudCambio(TipoCambio.CAMBIO, servicioId, origen.id(),
                    origen.medicoId(), receptor.id(), destino.id());
            ContextoValidacion contexto = new ContextoValidacion(hipotetica, hoy, origen, destino, cedente, receptor);
            if (reglas.evaluar(contexto).isEmpty()) {
                candidatos.add(new Candidato(receptor.id(), receptor.nombre(), destino.id()));
            }
        }
        candidatos.sort(Comparator.comparing(Candidato::nombre));
        return candidatos;
    }

    private Guardia guardiaDelServicio(long servicioId, long guardiaId) {
        return guardias.buscar(guardiaId)
                .filter(g -> g.servicioId() == servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La guardia no existe en este servicio."));
    }
}
