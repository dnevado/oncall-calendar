package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * FR-020 / CG-019 (data-model, «Efectos» §3): para cada fecha afectada, si el número de residentes
 * R3–R5 (tipo en esa fecha) pasa de 0 a más de 0, las guardias de refuerzo del día pasan a
 * localizada; si pasa de más de 0 a 0, las localizadas pasan a refuerzo. Es la acción del legacy
 * (`cambio_estado_guardia.jsp:206-226`), pero por día y sin depender de quién solicita.
 */
public class AjusteResidentes {

    /** Cambio de tipo de una guardia de adjunto. */
    public record Ajuste(long guardiaId, LocalDate fecha, TipoGuardia desde, TipoGuardia hasta) {
    }

    /**
     * @param guardiasPorFecha guardias del servicio en cada fecha afectada, antes del cambio
     * @param nuevoMedico médico que tendrá cada guardia que cambia de manos
     * @param tipoEn tipo de un médico en una fecha
     */
    public List<Ajuste> calcular(Map<LocalDate, List<Guardia>> guardiasPorFecha, Map<Long, Long> nuevoMedico,
            BiFunction<Long, LocalDate, Optional<TipoMedico>> tipoEn) {
        List<Ajuste> ajustes = new ArrayList<>();
        guardiasPorFecha.forEach((fecha, delDia) -> {
            long antes = delDia.stream().filter(g -> esR3aR5(tipoEn, g.medicoId(), fecha)).count();
            long despues = delDia.stream()
                    .filter(g -> esR3aR5(tipoEn, nuevoMedico.getOrDefault(g.id(), g.medicoId()), fecha))
                    .count();
            if (antes == 0 && despues > 0) {
                ajustes.addAll(cambiar(delDia, fecha, TipoGuardia.REFUERZO, TipoGuardia.LOCALIZADA));
            } else if (antes > 0 && despues == 0) {
                ajustes.addAll(cambiar(delDia, fecha, TipoGuardia.LOCALIZADA, TipoGuardia.REFUERZO));
            }
        });
        return ajustes;
    }

    private static boolean esR3aR5(BiFunction<Long, LocalDate, Optional<TipoMedico>> tipoEn, long medicoId,
            LocalDate fecha) {
        return tipoEn.apply(medicoId, fecha).map(TipoMedico::esR3aR5).orElse(false);
    }

    private static List<Ajuste> cambiar(List<Guardia> delDia, LocalDate fecha, TipoGuardia desde, TipoGuardia hasta) {
        return delDia.stream()
                .filter(g -> g.tipo() == desde)
                .map(g -> new Ajuste(g.id(), fecha, desde, hasta))
                .toList();
    }
}
