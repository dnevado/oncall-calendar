package es.medoncalls.guardias.vacaciones.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.application.ReglasIncumplidasException;
import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.Infraccion;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaFila;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.common.RecursoNoEncontradoException;
import es.medoncalls.guardias.vacaciones.domain.ModificacionVacaciones;
import es.medoncalls.guardias.vacaciones.domain.ReglasVacaciones;
import es.medoncalls.guardias.vacaciones.infrastructure.MedicoServicioFila;
import es.medoncalls.guardias.vacaciones.infrastructure.VacacionMapper;

/**
 * Añade y quita días de vacaciones de un médico en una única transacción (002 data-model,
 * «Operación modificar vacaciones»).
 */
@Service
public class ModificarVacacionesService {

    public static final String TEXTO_RECHAZO =
            "No se han guardado las vacaciones porque incumplen las reglas indicadas.";

    private final GuardiaMapper guardias;
    private final VacacionMapper vacaciones;
    private final ReglasVacaciones reglas;
    private final Clock clock;

    public ModificarVacacionesService(GuardiaMapper guardias, VacacionMapper vacaciones, ReglasVacaciones reglas,
            Clock clock) {
        this.guardias = guardias;
        this.vacaciones = vacaciones;
        this.reglas = reglas;
        this.clock = clock;
    }

    @Transactional
    public ResultadoModificacion modificar(long servicioId, long medicoId, List<LocalDate> anadir,
            List<LocalDate> quitar) {
        ModificacionVacaciones modificacion = ModificacionVacaciones.de(anadir, quitar);
        // Primera sentencia de la transacción: bloquear el servicio, como el registro de cambios de
        // la 001. Serializa las vacaciones con las cesiones del servicio (VAC-003 frente a CG-012), y
        // las lecturas posteriores ven lo ya confirmado (research V3).
        guardias.lockServicio(servicioId);
        MedicoServicioFila medico = vacaciones.medicosDelServicio(servicioId, medicoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("El médico no pertenece al servicio."));

        List<String> fechas = textos(modificacion.fechas());
        Map<LocalDate, Guardia> guardiasDelMedico = guardias
                .selectByMedicosFechasServicio(List.of(medicoId), fechas, servicioId).stream()
                .map(GuardiaFila::aGuardia)
                .collect(Collectors.toMap(Guardia::fecha, Function.identity(), (a, b) -> a));
        List<Infraccion> infracciones = reglas.evaluar(modificacion, LocalDate.now(clock), medicoId,
                medico.nombreCompleto(), guardiasDelMedico);
        if (!infracciones.isEmpty()) {
            throw new ReglasIncumplidasException(TEXTO_RECHAZO, infracciones);
        }

        Set<LocalDate> existentes = vacaciones.diasExistentes(medicoId, fechas).stream()
                .map(FechaLegacy::fecha).collect(Collectors.toSet());
        List<LocalDate> quitados = ReglasVacaciones.quitados(modificacion, existentes);
        List<LocalDate> anadidos = ReglasVacaciones.anadidos(modificacion, existentes);
        // En la misma transacción: si algo falla, no se quita ni se añade nada (FR-006).
        if (!quitados.isEmpty()) {
            vacaciones.borrarDias(medicoId, textos(quitados));
        }
        if (!anadidos.isEmpty()) {
            vacaciones.insertarDias(medicoId, textos(anadidos));
        }
        return new ResultadoModificacion(anadidos, quitados);
    }

    private static List<String> textos(Collection<LocalDate> fechas) {
        return fechas.stream().map(FechaLegacy::texto).toList();
    }
}
