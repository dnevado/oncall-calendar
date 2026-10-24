package es.medoncalls.guardias.cambios.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.AjusteResidentes.Ajuste;

/** FR-020 / CG-019: el ajuste depende del recuento de R3–R5 del día, no de quién solicita. */
class AjusteResidentesTest {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);
    private static final LocalDate DIA_15 = LocalDate.of(2030, 2, 15);
    private static final long R1 = 1;
    private static final long R2 = 2;
    private static final long R3 = 3;
    private static final long R4 = 4;
    private static final long ADJUNTO = 10;
    private static final long OTRO_ADJUNTO = 11;

    private final AjusteResidentes ajuste = new AjusteResidentes();
    private final Map<Long, TipoMedico> tipos = new HashMap<>();
    private long siguienteId = 100;

    @BeforeEach
    void plantilla() {
        tipos.put(R1, new TipoMedico(TipoMedico.Categoria.RESIDENTE, "R1"));
        tipos.put(R2, new TipoMedico(TipoMedico.Categoria.RESIDENTE, "R2"));
        tipos.put(R3, new TipoMedico(TipoMedico.Categoria.RESIDENTE, "R3"));
        tipos.put(R4, new TipoMedico(TipoMedico.Categoria.RESIDENTE, "R4"));
        tipos.put(ADJUNTO, new TipoMedico(TipoMedico.Categoria.ADJUNTO, null));
        tipos.put(OTRO_ADJUNTO, new TipoMedico(TipoMedico.Categoria.ADJUNTO, null));
    }

    private Guardia guardia(long medicoId, LocalDate fecha, TipoGuardia tipo) {
        return new Guardia(siguienteId++, medicoId, fecha, tipo, false, 1);
    }

    private List<Ajuste> calcular(Map<LocalDate, List<Guardia>> delDia, Map<Long, Long> nuevoMedico) {
        return ajuste.calcular(delDia, nuevoMedico, (medicoId, fecha) -> Optional.ofNullable(tipos.get(medicoId)));
    }

    @Test
    void escenario1_entraUnR3aR5_refuerzoPasaALocalizada() {
        Guardia residente = guardia(R2, DIA_10, TipoGuardia.RESIDENTE);
        Guardia refuerzo = guardia(ADJUNTO, DIA_10, TipoGuardia.REFUERZO);
        Guardia presencia = guardia(OTRO_ADJUNTO, DIA_10, TipoGuardia.PRESENCIA);

        List<Ajuste> ajustes = calcular(Map.of(DIA_10, List.of(residente, refuerzo, presencia)),
                Map.of(residente.id(), R4));

        assertThat(ajustes).containsExactly(
                new Ajuste(refuerzo.id(), DIA_10, TipoGuardia.REFUERZO, TipoGuardia.LOCALIZADA));
    }

    @Test
    void escenario2_saleElUnicoR3aR5_localizadaPasaARefuerzo() {
        Guardia residente = guardia(R3, DIA_10, TipoGuardia.RESIDENTE);
        Guardia localizada = guardia(ADJUNTO, DIA_10, TipoGuardia.LOCALIZADA);

        assertThat(calcular(Map.of(DIA_10, List.of(residente, localizada)), Map.of(residente.id(), R1)))
                .containsExactly(new Ajuste(localizada.id(), DIA_10, TipoGuardia.LOCALIZADA, TipoGuardia.REFUERZO));
    }

    @Test
    void escenario3_ambosR3aR5_oNinguno_sinCambios() {
        Guardia r3 = guardia(R3, DIA_10, TipoGuardia.RESIDENTE);
        Guardia localizada = guardia(ADJUNTO, DIA_10, TipoGuardia.LOCALIZADA);
        assertThat(calcular(Map.of(DIA_10, List.of(r3, localizada)), Map.of(r3.id(), R4))).isEmpty();

        Guardia r2 = guardia(R2, DIA_15, TipoGuardia.RESIDENTE);
        Guardia refuerzo = guardia(ADJUNTO, DIA_15, TipoGuardia.REFUERZO);
        assertThat(calcular(Map.of(DIA_15, List.of(r2, refuerzo)), Map.of(r2.id(), R1))).isEmpty();
    }

    @Test
    void escenario4_daIgualQueElR3aR5SeaElReceptor() {
        // El R4 recibe la guardia del R2 (es el receptor, no el solicitante): mismo resultado.
        Guardia residente = guardia(R2, DIA_10, TipoGuardia.RESIDENTE);
        Guardia refuerzo = guardia(ADJUNTO, DIA_10, TipoGuardia.REFUERZO);
        assertThat(calcular(Map.of(DIA_10, List.of(residente, refuerzo)), Map.of(residente.id(), R4)))
                .hasSize(1);

        // Y que sea el que entrega: el R3 cede al R1.
        Guardia r3 = guardia(R3, DIA_15, TipoGuardia.RESIDENTE);
        Guardia localizada = guardia(ADJUNTO, DIA_15, TipoGuardia.LOCALIZADA);
        assertThat(calcular(Map.of(DIA_15, List.of(r3, localizada)), Map.of(r3.id(), R1))).hasSize(1);
    }

    @Test
    void recuentoPorDia_siQuedaOtroR3aR5ElDiaNoCambia() {
        Guardia r3 = guardia(R3, DIA_10, TipoGuardia.RESIDENTE);
        Guardia r4 = guardia(R4, DIA_10, TipoGuardia.RESIDENTE);
        Guardia localizada = guardia(ADJUNTO, DIA_10, TipoGuardia.LOCALIZADA);
        assertThat(calcular(Map.of(DIA_10, List.of(r3, r4, localizada)), Map.of(r3.id(), R1))).isEmpty();
    }

    @Test
    void intercambio_seEvaluaCadaFechaPorSeparado() {
        // R4 el día 10 e R2 el día 15 se intercambian: el día 10 pierde su R3–R5 y el 15 lo gana.
        Guardia r4En10 = guardia(R4, DIA_10, TipoGuardia.RESIDENTE);
        Guardia localizada10 = guardia(ADJUNTO, DIA_10, TipoGuardia.LOCALIZADA);
        Guardia r2En15 = guardia(R2, DIA_15, TipoGuardia.RESIDENTE);
        Guardia refuerzo15 = guardia(OTRO_ADJUNTO, DIA_15, TipoGuardia.REFUERZO);

        List<Ajuste> ajustes = calcular(
                Map.of(DIA_10, List.of(r4En10, localizada10), DIA_15, List.of(r2En15, refuerzo15)),
                Map.of(r4En10.id(), R2, r2En15.id(), R4));

        assertThat(ajustes).containsExactlyInAnyOrder(
                new Ajuste(localizada10.id(), DIA_10, TipoGuardia.LOCALIZADA, TipoGuardia.REFUERZO),
                new Ajuste(refuerzo15.id(), DIA_15, TipoGuardia.REFUERZO, TipoGuardia.LOCALIZADA));
    }

    @Test
    void sinTipoConocidoNoCuentaComoR3aR5() {
        Guardia desconocido = guardia(99, DIA_10, TipoGuardia.RESIDENTE);
        Guardia refuerzo = guardia(ADJUNTO, DIA_10, TipoGuardia.REFUERZO);
        assertThat(calcular(Map.of(DIA_10, List.of(desconocido, refuerzo)), Map.of(desconocido.id(), R2)))
                .isEmpty();
    }
}
