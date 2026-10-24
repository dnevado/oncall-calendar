package es.medoncalls.guardias.cambios.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion;
import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.Infraccion;
import es.medoncalls.guardias.cambios.domain.ReglasCambio;
import es.medoncalls.guardias.cambios.domain.SolicitudCambio;
import es.medoncalls.guardias.cambios.domain.TipoCambio;
import es.medoncalls.guardias.cambios.infrastructure.CambioFila;
import es.medoncalls.guardias.cambios.infrastructure.CambioMapper;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.cambios.infrastructure.MedicoMapper;
import es.medoncalls.guardias.cambios.infrastructure.NuevoCambio;
import es.medoncalls.guardias.common.ConflictoException;
import es.medoncalls.guardias.common.PeticionInvalidaException;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;

/**
 * Registra y aplica un cambio en una única transacción (FR-002, FR-021, R9): bloquea las guardias
 * afectadas y después a quien las recibe (siempre en ese orden), carga la foto, evalúa todas las reglas y, si se cumplen, actualiza el calendario e
 * inserta el registro. Si algo falla no queda nada aplicado.
 */
@Service
public class RegistrarCambioService {

    private final GuardiaMapper guardias;
    private final MedicoMapper medicos;
    private final CambioMapper cambios;
    private final ContextoLoader contextos;
    private final ReglasCambio reglas;
    private final Clock clock;

    public RegistrarCambioService(GuardiaMapper guardias, MedicoMapper medicos, CambioMapper cambios,
            ContextoLoader contextos, ReglasCambio reglas, Clock clock) {
        this.guardias = guardias;
        this.medicos = medicos;
        this.cambios = cambios;
        this.contextos = contextos;
        this.reglas = reglas;
        this.clock = clock;
    }

    /**
     * @param administradorId usuario de la sesión, nunca un dato del cuerpo de la petición
     * @param claveIdempotencia cabecera `Idempotency-Key`, o null
     */
    @Transactional
    public CambioFila registrar(long administradorId, SolicitudCambio solicitud, String claveIdempotencia) {
        if (solicitud.tipo() != TipoCambio.CESION) {
            throw new PeticionInvalidaException("El registro de intercambios todavía no está disponible.");
        }
        // Primera sentencia de la transacción: bloquear. Las lecturas posteriores ven lo ya confirmado.
        Guardia origen = guardias.bloquear(List.of(solicitud.guardiaOrigenId())).stream()
                .filter(g -> g.servicioId() == solicitud.servicioId())
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("La guardia no existe en este servicio."));
        // El índice único cubre CG-011, pero no los días adyacentes de CG-013: bloquear también a
        // quien recibe, antes de cualquier lectura no bloqueante, para ver lo que otro cambio le dio.
        medicos.lockById(solicitud.receptorId());

        if (claveIdempotencia != null) {
            CambioFila previo = cambios.selectByIdempotencyKey(claveIdempotencia);
            if (previo != null) {
                if (!esLaMismaPeticion(previo, solicitud, origen)) {
                    throw new ConflictoException("La Idempotency-Key ya se usó con una petición distinta.");
                }
                return previo;
            }
        }

        ContextoValidacion contexto = contextos.cargar(solicitud, origen, null);
        List<Infraccion> infracciones = reglas.evaluar(contexto);
        if (!infracciones.isEmpty()) {
            throw new ReglasIncumplidasException(infracciones);
        }

        // FR-019: la guardia pasa al receptor; tipo y festivo se quedan con el día.
        try {
            guardias.updateMedico(origen.id(), solicitud.receptorId());
        } catch (DuplicateKeyException e) {
            // Otro cambio concurrente dio otra guardia al receptor ese día (UX_GUARDIA_MEDICO_DIA).
            throw new ReglasIncumplidasException(
                    List.of(reglas.otraGuardiaEseDia(contexto.receptor(), origen.fecha())));
        }

        String fechaOrigen = FechaLegacy.texto(origen.fecha());
        NuevoCambio nuevo = new NuevoCambio(TipoCambio.CESION.name(), fechaOrigen,
                fechaOrigen, // R10: en una CESION, FechaFinCambio = FechaIniCambio
                solicitud.cedenteId(), solicitud.receptorId(), FechaLegacy.texto(contextos.hoy()), administradorId,
                solicitud.servicioId(), origen.tipo().valorLegacy(), origen.festivo() ? 1 : 0, null, null,
                LocalDateTime.now(clock), claveIdempotencia);
        try {
            cambios.insert(nuevo);
        } catch (DuplicateKeyException e) {
            // La misma clave se registró a la vez desde otra petición.
            throw new ConflictoException("La Idempotency-Key ya se usó con una petición distinta.");
        }
        return cambios.selectById(nuevo.getId());
    }

    private static boolean esLaMismaPeticion(CambioFila previo, SolicitudCambio solicitud, Guardia origen) {
        return solicitud.tipo().name().equals(previo.tipoCambio())
                && Objects.equals(previo.servicioId(), solicitud.servicioId())
                && Objects.equals(previo.cedenteId(), solicitud.cedenteId())
                && Objects.equals(previo.receptorId(), solicitud.receptorId())
                && FechaLegacy.texto(origen.fecha()).equals(previo.fechaIniCambio());
    }
}
