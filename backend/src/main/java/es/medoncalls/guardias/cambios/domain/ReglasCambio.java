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
 * En este incremento cubre la CESION; el CAMBIO se añade con la historia 2.
 */
public class ReglasCambio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Mensajes mensajes;
    private final ReglaPresenciasConsecutivas presencias = new ReglaPresenciasConsecutivas();

    public ReglasCambio(Mensajes mensajes) {
        this.mensajes = mensajes;
    }

    public List<Infraccion> evaluar(ContextoValidacion contexto) {
        if (contexto.solicitud().tipo() != TipoCambio.CESION) {
            throw new UnsupportedOperationException("Reglas del CAMBIO pendientes (historia 2)");
        }
        SolicitudCambio solicitud = contexto.solicitud();
        Guardia origen = contexto.origen();
        Participante cedente = contexto.cedente();
        Participante receptor = contexto.receptor();
        List<Infraccion> infracciones = new ArrayList<>();

        // CG-005 (FR-005)
        if (origen.medicoId() != solicitud.cedenteId()) {
            infracciones.add(infraccion(ReglaCodigo.CG_005, "regla.CG-005", cedente, origen.fecha()));
        }
        // CG-008 (FR-008): estrictamente posterior a hoy.
        if (!origen.fecha().isAfter(contexto.hoy())) {
            infracciones.add(infraccion(ReglaCodigo.CG_008, "regla.CG-008", cedente, origen.fecha()));
        }
        // FR-015: con el mismo médico en ambos lados el resto de reglas no tiene sentido.
        if (solicitud.cedenteId() == solicitud.receptorId()) {
            infracciones.add(infraccion(ReglaCodigo.MISMO_MEDICO, "regla.MISMO_MEDICO", cedente, null));
            return infracciones;
        }

        infracciones.addAll(recibe(receptor, origen, null));
        // CG-014 (FR-013): mismo tipo en la fecha de la guardia.
        if (!mismoTipo(cedente, receptor, origen.fecha())) {
            infracciones.add(new Infraccion(ReglaCodigo.CG_014,
                    mensajes.texto("regla.CG-014", receptor.nombre(), texto(origen.fecha()), cedente.nombre()),
                    receptor.id(), origen.fecha()));
        }
        // CG-015 (FR-014): activo en general y miembro del servicio.
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

    /** Reglas de quien recibe una guardia: CG-011, CG-012 y CG-013. */
    private List<Infraccion> recibe(Participante quien, Guardia recibida, LocalDate otraFechaDelCambio) {
        List<Infraccion> infracciones = new ArrayList<>();
        LocalDate fecha = recibida.fecha();
        // CG-011 (FR-010): ninguna otra guardia ese día en el servicio.
        if (quien.guardiasEn(fecha).stream().anyMatch(g -> g.id() != recibida.id())) {
            infracciones.add(infraccion(ReglaCodigo.CG_011, "regla.CG-011", quien, fecha));
        }
        // CG-012 (FR-011): vacaciones en cualquier servicio.
        if (quien.deVacacionesEn(fecha)) {
            infracciones.add(infraccion(ReglaCodigo.CG_012, "regla.CG-012", quien, fecha));
        }
        // CG-013 (FR-012)
        infracciones.addAll(presencias.evaluar(quien, recibida, otraFechaDelCambio,
                (medico, dia) -> infraccion(ReglaCodigo.CG_013, "regla.CG-013", medico, dia)));
        return infracciones;
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
