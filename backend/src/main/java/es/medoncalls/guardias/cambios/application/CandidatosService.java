package es.medoncalls.guardias.cambios.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
 * ofrecen los médicos activos del servicio para los que las mismas reglas del registro no dan
 * ninguna infracción; el alta vuelve a validarlo todo.
 */
@Service
public class CandidatosService {

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

    @Transactional(readOnly = true)
    public List<MedicoFila> paraCesion(long servicioId, long guardiaId) {
        Guardia origen = guardias.buscar(guardiaId)
                .filter(g -> g.servicioId() == servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La guardia no existe en este servicio."));
        Set<LocalDate> fechas = ContextoValidacion.fechasRelevantes(origen.fecha(), null);
        Participante cedente = contextos.participante(origen.medicoId(), servicioId, fechas);
        LocalDate hoy = contextos.hoy();

        List<MedicoFila> candidatos = new ArrayList<>();
        for (MedicoFila medico : medicos.selectActivosDelServicio(servicioId)) {
            if (medico.id() == origen.medicoId()) {
                continue;
            }
            SolicitudCambio hipotetica = new SolicitudCambio(TipoCambio.CESION, servicioId, origen.id(),
                    origen.medicoId(), medico.id(), null);
            ContextoValidacion contexto = new ContextoValidacion(hipotetica, hoy, origen, null, cedente,
                    contextos.participante(medico.id(), servicioId, fechas));
            if (reglas.evaluar(contexto).isEmpty()) {
                candidatos.add(medico);
            }
        }
        return candidatos;
    }
}
