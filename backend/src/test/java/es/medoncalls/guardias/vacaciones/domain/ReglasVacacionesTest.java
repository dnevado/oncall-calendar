package es.medoncalls.guardias.vacaciones.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.Infraccion;
import es.medoncalls.guardias.cambios.domain.Mensajes;
import es.medoncalls.guardias.cambios.domain.ReglaCodigo;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;

/** VAC-003, VAC-004 y VAC-008 al añadir, y cálculo de los días realmente añadidos (002 US1). */
class ReglasVacacionesTest {

    private static final LocalDate HOY = LocalDate.of(2030, 1, 15);
    private static final long MEDICO = 7;
    private static final long SERVICIO = 3;

    /** Devuelve la clave y los argumentos, para comprobar qué texto se pide. */
    private static final Mensajes MENSAJES = (clave, args) -> clave + " " + String.join("|",
            java.util.Arrays.stream(args).map(String::valueOf).toList());

    private final ReglasVacaciones reglas = new ReglasVacaciones(MENSAJES);

    private static Guardia guardia(long id, LocalDate fecha) {
        return new Guardia(id, MEDICO, fecha, TipoGuardia.PRESENCIA, false, SERVICIO);
    }

    private List<Infraccion> evaluar(List<LocalDate> anadir, Map<LocalDate, Guardia> guardias) {
        return reglas.evaluar(ModificacionVacaciones.de(anadir, List.of()), HOY, MEDICO, "Bruno Prueba", guardias);
    }

    @Test
    void diasFuturosSinGuardiaNoIncumplenNada() {
        assertThat(evaluar(List.of(HOY.plusDays(1), HOY.plusDays(20)), Map.of())).isEmpty();
    }

    @Test
    void vac008_unDiaAnteriorAHoyEsInfraccion_yHoyNo() {
        List<Infraccion> infracciones = evaluar(List.of(HOY.minusDays(5), HOY), Map.of());

        assertThat(infracciones).hasSize(1);
        Infraccion i = infracciones.get(0);
        assertThat(i.regla()).isEqualTo(ReglaCodigo.VAC_008);
        assertThat(i.fecha()).isEqualTo(HOY.minusDays(5));
        assertThat(i.medicoId()).isEqualTo(MEDICO);
        assertThat(i.mensaje()).isEqualTo("regla.VAC-008 Bruno Prueba|10/01/2030|");
    }

    @Test
    void vac004_sinLimiteHaciaElFuturo() {
        assertThat(evaluar(List.of(HOY.plusYears(3)), Map.of())).isEmpty();
    }

    @Test
    void vac003_cadaDiaConGuardiaEnElServicioEsUnaInfraccion_todasALaVez() {
        LocalDate d10 = LocalDate.of(2030, 8, 10);
        LocalDate d12 = LocalDate.of(2030, 8, 12);
        List<Infraccion> infracciones = evaluar(List.of(d12, LocalDate.of(2030, 8, 11), d10),
                Map.of(d10, guardia(1, d10), d12, guardia(2, d12)));

        assertThat(infracciones).extracting(Infraccion::regla).containsExactly(ReglaCodigo.VAC_003, ReglaCodigo.VAC_003);
        assertThat(infracciones).extracting(Infraccion::fecha).containsExactly(d10, d12);
        assertThat(infracciones.get(0).mensaje()).isEqualTo("regla.VAC-003 Bruno Prueba|10/08/2030|presencia");
    }

    @Test
    void unDiaPasadoYConGuardiaDaLasDosInfracciones() {
        LocalDate ayer = HOY.minusDays(1);
        assertThat(evaluar(List.of(ayer), Map.of(ayer, guardia(1, ayer))))
                .extracting(Infraccion::regla).containsExactly(ReglaCodigo.VAC_008, ReglaCodigo.VAC_003);
    }

    @Test
    void anadidosSonLosQueNoExistian() {
        LocalDate d1 = HOY.plusDays(1);
        LocalDate d2 = HOY.plusDays(2);
        LocalDate d3 = HOY.plusDays(3);
        ModificacionVacaciones m = ModificacionVacaciones.de(List.of(d3, d1, d2), List.of());

        assertThat(ReglasVacaciones.anadidos(m, java.util.Set.of(d2))).containsExactly(d1, d3);
    }
}
