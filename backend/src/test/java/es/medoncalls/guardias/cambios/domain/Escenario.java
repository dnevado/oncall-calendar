package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.support.ResourceBundleMessageSource;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;

/** Construye contextos de validación ficticios para las pruebas unitarias de las reglas. */
final class Escenario {

    static final long SERVICIO = 1;
    static final LocalDate HOY = LocalDate.of(2030, 1, 15);
    static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);

    static final TipoMedico ADJUNTO = new TipoMedico(TipoMedico.Categoria.ADJUNTO, null);
    static final TipoMedico RESIDENTE_R2 = new TipoMedico(TipoMedico.Categoria.RESIDENTE, "R2");
    static final TipoMedico RESIDENTE_SIMULADO = new TipoMedico(TipoMedico.Categoria.RESIDENTE, "SIMULADO");

    static Mensajes mensajes() {
        ResourceBundleMessageSource fuente = new ResourceBundleMessageSource();
        fuente.setBasename("messages");
        fuente.setDefaultEncoding("UTF-8");
        fuente.setFallbackToSystemLocale(false);
        return (clave, argumentos) -> fuente.getMessage(clave, argumentos, Locale.forLanguageTag("es"));
    }

    private final AtomicLong ids = new AtomicLong(1000);

    final class Medico {
        final long id;
        final String nombre;
        boolean activo = true;
        boolean miembro = true;
        TipoMedico tipoPorDefecto;
        final Map<LocalDate, TipoMedico> tipos = new HashMap<>();
        final Map<LocalDate, List<Guardia>> guardias = new HashMap<>();
        final Set<LocalDate> vacaciones = new HashSet<>();

        Medico(long id, String nombre, TipoMedico tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipoPorDefecto = tipo;
        }

        Guardia guardia(LocalDate fecha, TipoGuardia tipo, boolean festivo) {
            Guardia g = new Guardia(ids.incrementAndGet(), id, fecha, tipo, festivo, SERVICIO);
            guardias.computeIfAbsent(fecha, f -> new ArrayList<>()).add(g);
            return g;
        }

        Participante participante(Set<LocalDate> fechas) {
            Map<LocalDate, TipoMedico> tiposEnFechas = new HashMap<>();
            for (LocalDate f : fechas) {
                TipoMedico t = tipos.getOrDefault(f, tipoPorDefecto);
                if (t != null) {
                    tiposEnFechas.put(f, t);
                }
            }
            return new Participante(id, nombre, activo, miembro, tiposEnFechas, Map.copyOf(guardias),
                    Set.copyOf(vacaciones));
        }
    }

    Medico medico(String nombre, TipoMedico tipo) {
        return new Medico(ids.incrementAndGet(), nombre, tipo);
    }

    ContextoValidacion cesion(Guardia origen, Medico cedente, Medico receptor) {
        SolicitudCambio solicitud = new SolicitudCambio(TipoCambio.CESION, SERVICIO, origen.id(), cedente.id,
                receptor.id, null);
        Set<LocalDate> fechas = ContextoValidacion.fechasRelevantes(origen.fecha(), null);
        return new ContextoValidacion(solicitud, HOY, origen, null, cedente.participante(fechas),
                receptor.participante(fechas));
    }
}
