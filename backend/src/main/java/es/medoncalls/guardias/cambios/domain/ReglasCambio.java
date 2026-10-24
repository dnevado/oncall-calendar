package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;

/**
 * Reglas de validación de un cambio (data-model, «Reglas de validación»). Devuelve todas las que
 * fallan (FR-016), cada una con el médico y la fecha afectados y un texto para el administrador.
 * Cubre la CESION (historia 1) y el CAMBIO (historia 2), en el que cada médico recibe la guardia
 * del otro.
 */
public class ReglasCambio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Mensajes mensajes;
    private final ReglaPresenciasConsecutivas presencias = new ReglaPresenciasConsecutivas();

    public ReglasCambio(Mensajes mensajes) {
        this.mensajes = mensajes;
    }

    public List<Infraccion> evaluar(ContextoValidacion contexto) {
        SolicitudCambio solicitud = contexto.solicitud();
        Guardia origen = contexto.origen();
        Guardia destino = contexto.destino();
        boolean esCambio = solicitud.tipo() == TipoCambio.CAMBIO;
        Participante cedente = contexto.cedente();
        Participante receptor = contexto.receptor();
        List<Infraccion> infracciones = new ArrayList<>();

        // CG-005 (FR-005)
        if (origen.medicoId() != solicitud.cedenteId()) {
            infracciones.add(infraccion(ReglaCodigo.CG_005, "regla.CG-005", cedente, origen.fecha()));
        }
        // CG-006 (FR-006): en un CAMBIO la guardia destino es del receptor.
        if (esCambio && destino.medicoId() != solicitud.receptorId()) {
            infracciones.add(infraccion(ReglaCodigo.CG_006, "regla.CG-006", receptor, destino.fecha()));
        }
        // CG-008 (FR-008): estrictamente posterior a hoy.
        if (!origen.fecha().isAfter(contexto.hoy())) {
            infracciones.add(infraccion(ReglaCodigo.CG_008, "regla.CG-008", cedente, origen.fecha()));
        }
        // CG-009 (FR-009): tampoco se reciben guardias de hoy o pasadas.
        if (esCambio && !destino.fecha().isAfter(contexto.hoy())) {
            infracciones.add(infraccion(ReglaCodigo.CG_009, "regla.CG-009", receptor, destino.fecha()));
        }
        // FR-015: con el mismo médico en ambos lados el resto de reglas no tiene sentido.
        if (solicitud.cedenteId() == solicitud.receptorId()) {
            infracciones.add(infraccion(ReglaCodigo.MISMO_MEDICO, "regla.MISMO_MEDICO", cedente, null));
            return infracciones;
        }

        // CG-011, CG-012 y CG-013 para cada médico que recibe una guardia.
        infracciones.addAll(recibe(receptor, origen, destino));
        if (esCambio) {
            infracciones.addAll(recibe(cedente, destino, origen));
        }
        // CG-014 (FR-013): mismo tipo en la fecha de cada guardia afectada.
        if (!mismoTipo(cedente, receptor, origen.fecha())) {
            infracciones.add(distintoTipo(receptor, cedente, origen.fecha()));
        }
        if (esCambio && !destino.fecha().equals(origen.fecha()) && !mismoTipo(cedente, receptor, destino.fecha())) {
            infracciones.add(distintoTipo(cedente, receptor, destino.fecha()));
        }
        // CG-015 (FR-014): el receptor, activo en general y miembro del servicio.
        if (!receptor.activo()) {
            infracciones.add(infraccion(ReglaCodigo.CG_015, "regla.CG-015.inactivo", receptor, null));
        } else if (!receptor.miembroDelServicio()) {
            infracciones.add(infraccion(ReglaCodigo.CG_015, "regla.CG-015.noMiembro", receptor, null));
        }
        return infracciones;
    }

    /** CG-011 detectada por el índice único al aplicar el cambio (otro cambio concurrente). */
    public Infraccion otraGuardiaEseDia(Participante quien, LocalDate fecha) {
        return infraccion(ReglaCodigo.CG_011, "regla.CG-011", quien, fecha);
    }

    /**
     * Reglas de quien recibe una guardia: CG-011, CG-012 y CG-013.
     *
     * @param entregada guardia que ese médico entrega en el mismo cambio, o null en una CESION
     */
    private List<Infraccion> recibe(Participante quien, Guardia recibida, Guardia entregada) {
        List<Infraccion> infracciones = new ArrayList<>();
        LocalDate fecha = recibida.fecha();
        // CG-011 (FR-010): ninguna otra guardia ese día en el servicio tras el cambio.
        if (quien.guardiasEn(fecha).stream()
                .anyMatch(g -> g.id() != recibida.id() && (entregada == null || g.id() != entregada.id()))) {
            infracciones.add(infraccion(ReglaCodigo.CG_011, "regla.CG-011", quien, fecha));
        }
        // CG-012 (FR-011): vacaciones en cualquier servicio.
        if (quien.deVacacionesEn(fecha)) {
            infracciones.add(infraccion(ReglaCodigo.CG_012, "regla.CG-012", quien, fecha));
        }
        // CG-013 (FR-012)
        infracciones.addAll(presencias.evaluar(quien, recibida, entregada == null ? null : entregada.fecha(),
                (medico, dia) -> infraccion(ReglaCodigo.CG_013, "regla.CG-013", medico, dia)));
        return infracciones;
    }

    private Infraccion distintoTipo(Participante quienRecibe, Participante otro, LocalDate fecha) {
        return new Infraccion(ReglaCodigo.CG_014,
                mensajes.texto("regla.CG-014", quienRecibe.nombre(), texto(fecha), otro.nombre()),
                quienRecibe.id(), fecha);
    }

    /** Sin tipo conocido en la fecha no se puede asegurar que coincidan: se rechaza. */
    private static boolean mismoTipo(Participante a, Participante b, LocalDate fecha) {
        Optional<TipoMedico> tipoA = a.tipoEn(fecha);
        Optional<TipoMedico> tipoB = b.tipoEn(fecha);
        return tipoA.isPresent() && tipoB.isPresent() && tipoA.get().mismaCategoria(tipoB.get());
    }

    private Infraccion infraccion(ReglaCodigo regla, String clave, Participante medico, LocalDate fecha) {
        return new Infraccion(regla, mensajes.texto(clave, medico.nombre(), fecha == null ? "" : texto(fecha)),
                medico.id(), fecha);
    }

    private static String texto(LocalDate fecha) {
        return FECHA.format(fecha);
    }
}
