package es.medoncalls.guardias.cambios.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.TipoMedico;

class HistoricoTipoFilaTest {

    private static final LocalDate DIA = LocalDate.of(2030, 2, 10);

    private static HistoricoTipoFila fila(String tipo, String subTipo, LocalDate desde, LocalDate hasta) {
        return new HistoricoTipoFila(1L, tipo, subTipo, desde, hasta);
    }

    @Test
    void desdeIncluidoYHastaExcluido() {
        List<HistoricoTipoFila> historico = List.of(
                fila("RESIDENTE", "R2", null, DIA),
                fila("ADJUNTO", null, DIA, null));
        assertThat(HistoricoTipoFila.tipoEn(historico, DIA.minusDays(1)))
                .contains(TipoMedico.desdeLegacy("RESIDENTE", "R2"));
        assertThat(HistoricoTipoFila.tipoEn(historico, DIA)).contains(TipoMedico.desdeLegacy("ADJUNTO", null));
    }

    @Test
    void siVariosEstanVigentesGanaElDeDesdeMasReciente() {
        List<HistoricoTipoFila> historico = List.of(
                fila("RESIDENTE", "R4", DIA.minusDays(3), null),
                fila("RESIDENTE", "R1", null, null),
                fila("RESIDENTE", "R3", DIA.minusDays(30), null));
        assertThat(HistoricoTipoFila.tipoEn(historico, DIA)).contains(TipoMedico.desdeLegacy("RESIDENTE", "R4"));
    }

    @Test
    void sinRegistroVigenteNoHayTipo() {
        assertThat(HistoricoTipoFila.tipoEn(List.of(fila("ADJUNTO", null, DIA.plusDays(1), null)), DIA)).isEmpty();
        assertThat(HistoricoTipoFila.tipoEn(List.of(), DIA)).isEmpty();
    }
}
