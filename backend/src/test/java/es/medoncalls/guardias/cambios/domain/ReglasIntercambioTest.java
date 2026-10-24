package es.medoncalls.guardias.cambios.domain;

import static es.medoncalls.guardias.cambios.domain.Escenario.ADJUNTO;
import static es.medoncalls.guardias.cambios.domain.Escenario.DIA_10;
import static es.medoncalls.guardias.cambios.domain.Escenario.HOY;
import static es.medoncalls.guardias.cambios.domain.Escenario.RESIDENTE_R2;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.Escenario.Medico;

/** Reglas del CAMBIO (historia 2): cada médico recibe la guardia del otro. */
class ReglasIntercambioTest {

    private static final LocalDate DIA_15 = DIA_10.plusDays(5);

    private final ReglasCambio reglas = new ReglasCambio(Escenario.mensajes());
    private Escenario escenario;
    private Medico ana;
    private Medico bruno;
    private Guardia localizada10;
    private Guardia presencia15;

    @BeforeEach
    void calendario() {
        escenario = new Escenario();
        ana = escenario.medico("Ana Prueba", ADJUNTO);
        bruno = escenario.medico("Bruno Prueba", ADJUNTO);
        localizada10 = ana.guardia(DIA_10, TipoGuardia.LOCALIZADA, false);
        presencia15 = bruno.guardia(DIA_15, TipoGuardia.PRESENCIA, true);
    }

    private List<Infraccion> evaluar(Guardia origen, Medico cedente, Guardia destino, Medico receptor) {
        return reglas.evaluar(escenario.intercambio(origen, cedente, destino, receptor));
    }

    private List<Infraccion> evaluar() {
        return evaluar(localizada10, ana, presencia15, bruno);
    }

    private static List<ReglaCodigo> codigos(List<Infraccion> infracciones) {
        return infracciones.stream().map(Infraccion::regla).toList();
    }

    @Test
    void intercambioValidoNoTieneInfracciones() {
        assertThat(evaluar()).isEmpty();
    }

    @Test
    void cg006_elReceptorNoTieneLaGuardiaDestino() {
        Medico carla = escenario.medico("Carla Prueba", ADJUNTO);
        Guardia deCarla = carla.guardia(DIA_15.plusDays(2), TipoGuardia.LOCALIZADA, false);
        List<Infraccion> infracciones = evaluar(localizada10, ana, deCarla, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_006);
        Infraccion i = infracciones.get(0);
        assertThat(i.medicoId()).isEqualTo(bruno.id);
        assertThat(i.fecha()).isEqualTo(DIA_15.plusDays(2));
        assertThat(i.mensaje()).isEqualTo("Bruno Prueba no tiene guardia el 17/02/2030.");
    }

    @Test
    void cg009_laFechaDestinoDebeSerPosteriorAHoy() {
        Guardia deHoy = bruno.guardia(HOY, TipoGuardia.LOCALIZADA, false);
        Guardia pasada = bruno.guardia(HOY.minusDays(3), TipoGuardia.LOCALIZADA, false);

        assertThat(codigos(evaluar(localizada10, ana, deHoy, bruno))).containsExactly(ReglaCodigo.CG_009);
        assertThat(codigos(evaluar(localizada10, ana, pasada, bruno))).containsExactly(ReglaCodigo.CG_009);
    }

    @Test
    void cg008YCg009SeInformanJuntas() {
        Guardia pasadaAna = ana.guardia(HOY.minusDays(1), TipoGuardia.LOCALIZADA, false);
        Guardia pasadaBruno = bruno.guardia(HOY.minusDays(2), TipoGuardia.LOCALIZADA, false);
        assertThat(codigos(evaluar(pasadaAna, ana, pasadaBruno, bruno)))
                .containsExactly(ReglaCodigo.CG_008, ReglaCodigo.CG_009);
    }

    @Test
    void cg011_paraLosDosMedicos() {
        bruno.guardia(DIA_10, TipoGuardia.REFUERZO, false);
        ana.guardia(DIA_15, TipoGuardia.REFUERZO, false);
        List<Infraccion> infracciones = evaluar();

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_011, ReglaCodigo.CG_011);
        assertThat(infracciones.get(0).medicoId()).isEqualTo(bruno.id);
        assertThat(infracciones.get(0).fecha()).isEqualTo(DIA_10);
        assertThat(infracciones.get(1).medicoId()).isEqualTo(ana.id);
        assertThat(infracciones.get(1).fecha()).isEqualTo(DIA_15);
    }

    @Test
    void cg011_unIntercambioEnElMismoDiaNoCuentaLaGuardiaQueSeEntrega() {
        Guardia presenciaBruno10 = bruno.guardia(DIA_10, TipoGuardia.PRESENCIA, false);
        assertThat(evaluar(localizada10, ana, presenciaBruno10, bruno)).isEmpty();
    }

    @Test
    void cg012_paraLosDosMedicos() {
        bruno.vacaciones.add(DIA_10);
        ana.vacaciones.add(DIA_15);
        List<Infraccion> infracciones = evaluar();

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_012, ReglaCodigo.CG_012);
        assertThat(infracciones.get(0).medicoId()).isEqualTo(bruno.id);
        assertThat(infracciones.get(1).medicoId()).isEqualTo(ana.id);
    }

    @Test
    void cg013_escenario4_quienRecibeLaPresenciaTienePresenciaElDiaAnterior() {
        ana.guardia(DIA_15.minusDays(1), TipoGuardia.PRESENCIA, false);
        List<Infraccion> infracciones = evaluar();

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_013);
        assertThat(infracciones.get(0).medicoId()).isEqualTo(ana.id);
        assertThat(infracciones.get(0).fecha()).isEqualTo(DIA_15.minusDays(1));
    }

    @Test
    void cg013_escenario5_elDiaAdyacenteQueEsLaOtraFechaDelCambioNoCuenta() {
        Guardia presencia10 = ana.guardia(DIA_10.minusDays(20), TipoGuardia.PRESENCIA, false);
        Guardia presencia11 = bruno.guardia(DIA_10.minusDays(19), TipoGuardia.PRESENCIA, false);
        // Ambas fechas son posteriores a hoy y adyacentes; cada uno entrega justo la del día vecino.
        assertThat(evaluar(presencia10, ana, presencia11, bruno)).isEmpty();
    }

    @Test
    void cg013_paraElReceptorDeLaFechaOrigen() {
        Guardia presenciaAna10 = ana.guardia(DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false);
        bruno.guardia(DIA_10.plusDays(2), TipoGuardia.PRESENCIA, false);
        List<Infraccion> infracciones = evaluar(presenciaAna10, ana, presencia15, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_013);
        assertThat(infracciones.get(0).medicoId()).isEqualTo(bruno.id);
    }

    @Test
    void cg014_seComparaElTipoEnLasDosFechas() {
        bruno.tipos.put(DIA_15, RESIDENTE_R2);
        bruno.tipos.put(DIA_15.minusDays(1), RESIDENTE_R2);
        bruno.tipos.put(DIA_15.plusDays(1), RESIDENTE_R2);
        List<Infraccion> infracciones = evaluar();

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_014);
        assertThat(infracciones.get(0).fecha()).isEqualTo(DIA_15);
    }

    @Test
    void cg015_receptorInactivo() {
        bruno.activo = false;
        assertThat(codigos(evaluar())).containsExactly(ReglaCodigo.CG_015);
    }

    @Test
    void mismoMedicoEnAmbosLados() {
        Guardia otraDeAna = ana.guardia(DIA_15, TipoGuardia.PRESENCIA, false);
        assertThat(codigos(evaluar(localizada10, ana, otraDeAna, ana))).containsExactly(ReglaCodigo.MISMO_MEDICO);
    }
}
