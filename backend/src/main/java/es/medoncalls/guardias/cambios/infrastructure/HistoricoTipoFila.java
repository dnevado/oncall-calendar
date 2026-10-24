package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import es.medoncalls.guardias.cambios.domain.TipoMedico;

/** Registro de `medicos_tipo_historico`: tipo vigente en [desde, hasta); null = sin límite (FR-024). */
public record HistoricoTipoFila(Long medicoId, String tipo, String subTipo, LocalDate desde, LocalDate hasta) {

    /** Sin desde se ordena como el más antiguo, igual que NULL en un ORDER BY de MySQL. */
    private static final Comparator<HistoricoTipoFila> MAS_RECIENTE_PRIMERO = Comparator
            .comparing(HistoricoTipoFila::desde, Comparator.nullsFirst(Comparator.<LocalDate>naturalOrder()))
            .reversed();

    public boolean vigenteEn(LocalDate fecha) {
        return (desde == null || !desde.isAfter(fecha)) && (hasta == null || hasta.isAfter(fecha));
    }

    /** Tipo en la fecha: de los registros vigentes, el de `desde` más reciente. */
    public static Optional<TipoMedico> tipoEn(List<HistoricoTipoFila> historico, LocalDate fecha) {
        return historico.stream()
                .filter(h -> h.vigenteEn(fecha))
                .sorted(MAS_RECIENTE_PRIMERO)
                .findFirst()
                .map(h -> TipoMedico.desdeLegacy(h.tipo(), h.subTipo()));
    }
}
