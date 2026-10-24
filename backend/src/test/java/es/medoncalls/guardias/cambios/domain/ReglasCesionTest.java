package es.medoncalls.guardias.cambios.domain;

import static es.medoncalls.guardias.cambios.domain.Escenario.ADJUNTO;
import static es.medoncalls.guardias.cambios.domain.Escenario.DIA_10;
import static es.medoncalls.guardias.cambios.domain.Escenario.HOY;
import static es.medoncalls.guardias.cambios.domain.Escenario.RESIDENTE_R2;
import static es.medoncalls.guardias.cambios.domain.Escenario.RESIDENTE_SIMULADO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.Escenario.Medico;

/** Reglas de la CESION (historia 1; CG-005, CG-007, CG-008, CG-011–CG-015, MISMO_MEDICO). */
class ReglasCesionTest {

    private final ReglasCambio reglas = new ReglasCambio(Escenario.mensajes());
    private Escenario escenario;
    private Medico ana;
    private Medico bruno;
    private Guardia presencia10;

    @BeforeEach
    void calendario() {
        escenario = new Escenario();
        ana = escenario.medico("Ana Prueba", ADJUNTO);
        bruno = escenario.medico("Bruno Prueba", ADJUNTO);
        presencia10 = ana.guardia(DIA_10, TipoGuardia.PRESENCIA, true);
    }

    private List<Infraccion> evaluar(Guardia origen, Medico cedente, Medico receptor) {
        return reglas.evaluar(escenario.cesion(origen, cedente, receptor));
    }

    private static List<ReglaCodigo> codigos(List<Infraccion> infracciones) {
        return infracciones.stream().map(Infraccion::regla).toList();
    }

    @Test
    void cesionValidaNoTieneInfracciones() {
        assertThat(evaluar(presencia10, ana, bruno)).isEmpty();
    }

    @Test
    void cg007_elReceptorSinNingunaOtraGuardiaEsValido() {
        assertThat(bruno.guardias).isEmpty();
        assertThat(evaluar(presencia10, ana, bruno)).isEmpty();
    }

    @Test
    void cg005_laGuardiaYaNoEsDelCedente() {
        Medico carla = escenario.medico("Carla Prueba", ADJUNTO);
        List<Infraccion> infracciones = evaluar(presencia10, carla, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_005);
        Infraccion i = infracciones.get(0);
        assertThat(i.medicoId()).isEqualTo(carla.id);
        assertThat(i.fecha()).isEqualTo(DIA_10);
        assertThat(i.mensaje()).isEqualTo("La guardia del 10/02/2030 ya no es de Carla Prueba.");
    }

    @Test
    void cg008_hoyYFechasPasadasSeRechazan() {
        Guardia deHoy = ana.guardia(HOY, TipoGuardia.LOCALIZADA, false);
        Guardia deAyer = ana.guardia(HOY.minusDays(1), TipoGuardia.LOCALIZADA, false);

        assertThat(codigos(evaluar(deHoy, ana, bruno))).containsExactly(ReglaCodigo.CG_008);
        assertThat(codigos(evaluar(deAyer, ana, bruno))).containsExactly(ReglaCodigo.CG_008);
        assertThat(evaluar(ana.guardia(HOY.plusDays(1), TipoGuardia.LOCALIZADA, false), ana, bruno)).isEmpty();
    }

    @Test
    void cg011_elReceptorYaTieneGuardiaEseDia() {
        bruno.guardia(DIA_10, TipoGuardia.LOCALIZADA, false);
        List<Infraccion> infracciones = evaluar(presencia10, ana, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_011);
        assertThat(infracciones.get(0).mensaje()).isEqualTo("Bruno Prueba ya tiene otra guardia el 10/02/2030.");
    }

    @Test
    void cg012_elReceptorEstaDeVacaciones() {
        bruno.vacaciones.add(DIA_10);
        List<Infraccion> infracciones = evaluar(presencia10, ana, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_012);
        assertThat(infracciones.get(0).medicoId()).isEqualTo(bruno.id);
        assertThat(infracciones.get(0).mensaje()).isEqualTo("Bruno Prueba está de vacaciones el 10/02/2030.");
    }

    @Test
    void cg013_presenciaElDiaAnteriorOPosterior() {
        bruno.guardia(DIA_10.minusDays(1), TipoGuardia.PRESENCIA, false);
        bruno.guardia(DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false);
        List<Infraccion> infracciones = evaluar(presencia10, ana, bruno);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_013, ReglaCodigo.CG_013);
        assertThat(infracciones).extracting(Infraccion::fecha)
                .containsExactly(DIA_10.minusDays(1), DIA_10.plusDays(1));
        assertThat(infracciones.get(0).mensaje()).isEqualTo(
                "Bruno Prueba tiene guardia de presencia el 09/02/2030, día consecutivo a la presencia que recibiría.");
    }

    @Test
    void cg013_elResidenteSimuladoNoEstaExento() {
        Medico rita = escenario.medico("Rita Prueba", RESIDENTE_SIMULADO);
        Medico sara = escenario.medico("Sara Prueba", RESIDENTE_SIMULADO);
        Guardia presenciaRita = rita.guardia(DIA_10, TipoGuardia.PRESENCIA, false);
        sara.guardia(DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false);

        assertThat(codigos(evaluar(presenciaRita, rita, sara))).containsExactly(ReglaCodigo.CG_013);
    }

    @Test
    void cg013_soloAfectaAPresencias() {
        Guardia localizada = ana.guardia(DIA_10.plusDays(5), TipoGuardia.LOCALIZADA, false);
        bruno.guardia(DIA_10.plusDays(4), TipoGuardia.PRESENCIA, false);
        assertThat(evaluar(localizada, ana, bruno)).isEmpty();

        bruno.guardia(DIA_10.minusDays(1), TipoGuardia.LOCALIZADA, false);
        bruno.guardia(DIA_10.plusDays(1), TipoGuardia.REFUERZO, false);
        assertThat(evaluar(presencia10, ana, bruno)).isEmpty();
    }

    @Test
    void cg014_tipoDistintoEnLaFechaDeLaGuardia() {
        Medico rita = escenario.medico("Rita Prueba", RESIDENTE_R2);
        List<Infraccion> infracciones = evaluar(presencia10, ana, rita);

        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.CG_014);
        assertThat(infracciones.get(0).mensaje())
                .isEqualTo("Rita Prueba y Ana Prueba no son del mismo tipo de médico el 10/02/2030.");
    }

    @Test
    void cg014_seEvaluaConElTipoQueTeniaEnEsaFecha() {
        // Hoy es residente, pero el 10/02 ya será adjunta según el histórico.
        Medico rita = escenario.medico("Rita Prueba", RESIDENTE_R2);
        rita.tipos.put(DIA_10, ADJUNTO);
        assertThat(evaluar(presencia10, ana, rita)).isEmpty();
    }

    @Test
    void cg014_sinTipoConocidoSeRechaza() {
        Medico sinHistorico = escenario.medico("Sin Histórico", null);
        assertThat(codigos(evaluar(presencia10, ana, sinHistorico))).containsExactly(ReglaCodigo.CG_014);
    }

    @Test
    void cg015_receptorInactivoONoMiembroSeRechaza() {
        bruno.activo = false;
        List<Infraccion> inactivo = evaluar(presencia10, ana, bruno);
        assertThat(codigos(inactivo)).containsExactly(ReglaCodigo.CG_015);
        assertThat(inactivo.get(0).mensaje()).isEqualTo("Bruno Prueba no está activo.");

        bruno.activo = true;
        bruno.miembro = false;
        List<Infraccion> noMiembro = evaluar(presencia10, ana, bruno);
        assertThat(codigos(noMiembro)).containsExactly(ReglaCodigo.CG_015);
        assertThat(noMiembro.get(0).mensaje()).isEqualTo("Bruno Prueba no pertenece al servicio.");
    }

    @Test
    void cg015_activoEnGeneralYMiembroSeAceptaAunqueNoEsteActivoEnElServicio() {
        // ActivoServicio no forma parte del contexto: pertenecer = existir la fila (CG-015).
        bruno.activo = true;
        bruno.miembro = true;
        assertThat(evaluar(presencia10, ana, bruno)).isEmpty();
    }

    @Test
    void mismoMedico() {
        List<Infraccion> infracciones = evaluar(presencia10, ana, ana);
        assertThat(codigos(infracciones)).containsExactly(ReglaCodigo.MISMO_MEDICO);
        assertThat(infracciones.get(0).mensaje())
                .isEqualTo("El médico que cede y el que recibe son la misma persona (Ana Prueba).");
    }

    @Test
    void devuelveTodasLasInfraccionesALaVez() {
        Medico rita = escenario.medico("Rita Prueba", RESIDENTE_R2);
        rita.activo = false;
        rita.vacaciones.add(DIA_10);
        rita.guardia(DIA_10, TipoGuardia.RESIDENTE, false);
        rita.guardia(DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false);

        assertThat(codigos(evaluar(presencia10, ana, rita))).containsExactlyInAnyOrder(
                ReglaCodigo.CG_011, ReglaCodigo.CG_012, ReglaCodigo.CG_013, ReglaCodigo.CG_014, ReglaCodigo.CG_015);
    }
}
