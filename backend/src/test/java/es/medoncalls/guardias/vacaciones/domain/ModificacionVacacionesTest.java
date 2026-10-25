package es.medoncalls.guardias.vacaciones.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.common.PeticionInvalidaException;

/** Forma de la petición de modificar vacaciones (002 research V7; 400). */
class ModificacionVacacionesTest {

    private static final LocalDate D1 = LocalDate.of(2030, 2, 10);
    private static final LocalDate D2 = LocalDate.of(2030, 2, 11);

    @Test
    void aceptaListasValidasYAusentes() {
        ModificacionVacaciones m = ModificacionVacaciones.de(List.of(D2, D1), null);
        assertThat(m.anadir()).containsExactly(D1, D2);
        assertThat(m.quitar()).isEmpty();
    }

    @Test
    void sinNingunaFechaEsInvalida() {
        assertThatThrownBy(() -> ModificacionVacaciones.de(List.of(), List.of()))
                .isInstanceOf(PeticionInvalidaException.class);
        assertThatThrownBy(() -> ModificacionVacaciones.de(null, null))
                .isInstanceOf(PeticionInvalidaException.class);
    }

    @Test
    void unaFechaEnLasDosListasEsInvalida() {
        assertThatThrownBy(() -> ModificacionVacaciones.de(List.of(D1), List.of(D2, D1)))
                .isInstanceOf(PeticionInvalidaException.class)
                .hasMessageContaining("2030-02-10");
    }

    @Test
    void unaFechaRepetidaEnUnaListaEsInvalida() {
        assertThatThrownBy(() -> ModificacionVacaciones.de(List.of(D1, D1), List.of()))
                .isInstanceOf(PeticionInvalidaException.class);
        assertThatThrownBy(() -> ModificacionVacaciones.de(List.of(), List.of(D2, D2)))
                .isInstanceOf(PeticionInvalidaException.class);
    }

    @Test
    void masDe366FechasPorListaEsInvalida() {
        List<LocalDate> fechas367 = IntStream.range(0, 367).mapToObj(D1::plusDays).toList();
        assertThatThrownBy(() -> ModificacionVacaciones.de(fechas367, List.of()))
                .isInstanceOf(PeticionInvalidaException.class);
        assertThat(ModificacionVacaciones.de(fechas367.subList(0, 366), List.of()).anadir()).hasSize(366);
    }

    @Test
    void unaFechaNulaEsInvalida() {
        assertThatThrownBy(() -> ModificacionVacaciones.de(java.util.Arrays.asList(D1, null), List.of()))
                .isInstanceOf(PeticionInvalidaException.class);
    }
}
