package es.medoncalls.guardias.cambios.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.domain.AjusteResidentes;
import es.medoncalls.guardias.cambios.domain.AjusteResidentes.Ajuste;
import es.medoncalls.guardias.cambios.domain.ContextoValidacion;
import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;
import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.Infraccion;
import es.medoncalls.guardias.cambios.domain.ReglasCambio;
import es.medoncalls.guardias.cambios.domain.SolicitudCambio;
import es.medoncalls.guardias.cambios.domain.TipoCambio;
import es.medoncalls.guardias.cambios.infrastructure.CambioFila;
import es.medoncalls.guardias.cambios.infrastructure.CambioMapper;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.cambios.infrastructure.HistoricoTipoFila;
import es.medoncalls.guardias.cambios.infrastructure.NuevoCambio;
import es.medoncalls.guardias.common.ConflictoException;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;

/**
 * Registra y aplica un cambio en una única transacción (FR-002, FR-021, R9): bloquea el servicio y
 * las guardias afectadas (siempre en ese orden), carga la foto, evalúa todas las reglas y, si se
 * cumplen, actualiza el calendario (incluido el ajuste R3–R5) e inserta el registro. Si algo falla
 * no queda nada aplicado.
 */
@Service
public class RegistrarCambioService {

    private final GuardiaMapper guardias;
    private final CambioMapper cambios;
    private final ContextoLoader contextos;
    private final ReglasCambio reglas;
    private final AjusteResidentes ajusteResidentes;
    private final Clock clock;

    public RegistrarCambioService(GuardiaMapper guardias, CambioMapper cambios, ContextoLoader contextos,
            ReglasCambio reglas, AjusteResidentes ajusteResidentes, Clock clock) {
        this.guardias = guardias;
        this.cambios = cambios;
        this.contextos = contextos;
        this.reglas = reglas;
        this.ajusteResidentes = ajusteResidentes;
        this.clock = clock;
    }

    /**
     * @param administradorId usuario de la sesión, nunca un dato del cuerpo de la petición
     * @param claveIdempotencia cabecera `Idempotency-Key`, o null
     */
    @Transactional
    public CambioFila registrar(long administradorId, SolicitudCambio solicitud, String claveIdempotencia) {
        // Primera sentencia de la transacción: bloquear el servicio. Serializa los cambios del
        // servicio, de modo que CG-011, CG-013 y el recuento de residentes de cada día (FR-020) no
        // cambian entre la lectura y la escritura. Las lecturas posteriores ven lo ya confirmado.
        guardias.lockServicio(solicitud.servicioId());
        boolean esCambio = solicitud.tipo() == TipoCambio.CAMBIO;
        // Y las guardias, por si el legacy las modifica a la vez.
        Map<Long, Guardia> bloqueadas = guardias.bloquear(esCambio
                        ? List.of(solicitud.guardiaOrigenId(), solicitud.guardiaDestinoId())
                        : List.of(solicitud.guardiaOrigenId())).stream()
                .filter(g -> g.servicioId() == solicitud.servicioId())
                .collect(Collectors.toMap(Guardia::id, Function.identity()));
        Guardia origen = existente(bloqueadas, solicitud.guardiaOrigenId());
        Guardia destino = esCambio ? existente(bloqueadas, solicitud.guardiaDestinoId()) : null;

        if (claveIdempotencia != null) {
            CambioFila previo = cambios.selectByIdempotencyKey(claveIdempotencia);
            if (previo != null) {
                if (!esLaMismaPeticion(previo, solicitud, origen, destino)) {
                    throw new ConflictoException("La Idempotency-Key ya se usó con una petición distinta.");
                }
                return previo;
            }
        }

        ContextoValidacion contexto = contextos.cargar(solicitud, origen, destino);
        List<Infraccion> infracciones = reglas.evaluar(contexto);
        if (!infracciones.isEmpty()) {
            throw new ReglasIncumplidasException(infracciones);
        }

        // FR-020: se calcula con el calendario de antes del cambio, que es el que se acaba de leer.
        List<Ajuste> ajustes = ajustesR3aR5(solicitud, origen, destino);

        if (esCambio) {
            // FR-018: se intercambian los médicos; tipo y festivo se quedan con cada día. El índice
            // único se comprueba fila a fila: en un intercambio del mismo día, la guardia origen pasa
            // antes por un médico provisional (el receptor en negativo) para no chocar a mitad.
            if (origen.fecha().equals(destino.fecha())) {
                guardias.updateMedico(origen.id(), -solicitud.receptorId());
            }
            asignar(destino, contexto.cedente());
            asignar(origen, contexto.receptor());
        } else {
            // FR-019: la guardia pasa al receptor; tipo y festivo se quedan con el día.
            asignar(origen, contexto.receptor());
        }
        for (Ajuste ajuste : ajustes) {
            guardias.cambiarTipo(ajuste.guardiaId(), ajuste.hasta());
        }

        String fechaOrigen = FechaLegacy.texto(origen.fecha());
        NuevoCambio nuevo = new NuevoCambio(solicitud.tipo().name(), fechaOrigen,
                // R10: en una CESION, FechaFinCambio = FechaIniCambio
                esCambio ? FechaLegacy.texto(destino.fecha()) : fechaOrigen,
                solicitud.cedenteId(), solicitud.receptorId(), FechaLegacy.texto(contextos.hoy()), administradorId,
                solicitud.servicioId(), origen.tipo().valorLegacy(), origen.festivo() ? 1 : 0,
                esCambio ? destino.tipo().valorLegacy() : null, esCambio ? (destino.festivo() ? 1 : 0) : null,
                LocalDateTime.now(clock), claveIdempotencia);
        try {
            cambios.insert(nuevo);
        } catch (DuplicateKeyException e) {
            // La misma clave se registró a la vez desde otra petición.
            throw new ConflictoException("La Idempotency-Key ya se usó con una petición distinta.");
        }
        return cambios.selectById(nuevo.getId());
    }

    private List<Ajuste> ajustesR3aR5(SolicitudCambio solicitud, Guardia origen, Guardia destino) {
        Map<Long, Long> nuevoMedico = destino == null
                ? Map.of(origen.id(), solicitud.receptorId())
                : Map.of(origen.id(), solicitud.receptorId(), destino.id(), solicitud.cedenteId());
        Map<LocalDate, List<Guardia>> guardiasPorFecha = new TreeMap<>();
        guardiasPorFecha.put(origen.fecha(), guardias.delServicioEnFecha(solicitud.servicioId(), origen.fecha()));
        if (destino != null) {
            guardiasPorFecha.computeIfAbsent(destino.fecha(),
                    f -> guardias.delServicioEnFecha(solicitud.servicioId(), f));
        }
        Set<Long> medicos = new HashSet<>(nuevoMedico.values());
        guardiasPorFecha.values().forEach(delDia -> delDia.forEach(g -> medicos.add(g.medicoId())));
        Map<Long, List<HistoricoTipoFila>> historicos = contextos.historicos(medicos);
        return ajusteResidentes.calcular(guardiasPorFecha, nuevoMedico,
                (medicoId, fecha) -> HistoricoTipoFila.tipoEn(historicos.getOrDefault(medicoId, List.of()), fecha));
    }

    private void asignar(Guardia guardia, Participante nuevoMedico) {
        try {
            guardias.updateMedico(guardia.id(), nuevoMedico.id());
        } catch (DuplicateKeyException e) {
            // El legacy dio otra guardia a ese médico ese día (UX_GUARDIA_MEDICO_DIA).
            throw new ReglasIncumplidasException(List.of(reglas.otraGuardiaEseDia(nuevoMedico, guardia.fecha())));
        }
    }

    private static Guardia existente(Map<Long, Guardia> bloqueadas, long guardiaId) {
        Guardia guardia = bloqueadas.get(guardiaId);
        if (guardia == null) {
            throw new RecursoNoEncontradoException("La guardia " + guardiaId + " no existe en este servicio.");
        }
        return guardia;
    }

    private static boolean esLaMismaPeticion(CambioFila previo, SolicitudCambio solicitud, Guardia origen,
            Guardia destino) {
        return solicitud.tipo().name().equals(previo.tipoCambio())
                && Objects.equals(previo.servicioId(), solicitud.servicioId())
                && Objects.equals(previo.cedenteId(), solicitud.cedenteId())
                && Objects.equals(previo.receptorId(), solicitud.receptorId())
                && FechaLegacy.texto(origen.fecha()).equals(previo.fechaIniCambio())
                && (destino == null || FechaLegacy.texto(destino.fecha()).equals(previo.fechaFinCambio()));
    }
}
