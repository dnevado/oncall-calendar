package es.medoncalls.guardias.vacaciones.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import es.medoncalls.guardias.common.PeticionInvalidaException;

/**
 * Días que se añaden y días que se quitan de las vacaciones de un médico (002 research V2). Solo
 * se aplican esos días; el resto no cambia (VAC-007).
 */
public record ModificacionVacaciones(SortedSet<LocalDate> anadir, SortedSet<LocalDate> quitar) {

    /** Límite técnico por lista, no regla de negocio (research V7): un año de días cabe en una petición. */
    public static final int MAX_FECHAS = 366;

    public ModificacionVacaciones {
        anadir = new TreeSet<>(anadir);
        quitar = new TreeSet<>(quitar);
    }

    /**
     * Comprueba la forma de la petición antes de convertir las listas en conjuntos, para no perder
     * los repetidos (400).
     */
    public static ModificacionVacaciones de(List<LocalDate> anadir, List<LocalDate> quitar) {
        SortedSet<LocalDate> a = conjunto(anadir, "anadir");
        SortedSet<LocalDate> q = conjunto(quitar, "quitar");
        if (a.isEmpty() && q.isEmpty()) {
            throw new PeticionInvalidaException("No se ha indicado ningún día que añadir o quitar.");
        }
        Set<LocalDate> enLasDos = new TreeSet<>(a);
        enLasDos.retainAll(q);
        if (!enLasDos.isEmpty()) {
            throw new PeticionInvalidaException("Un mismo día no se puede añadir y quitar a la vez: " + enLasDos + ".");
        }
        return new ModificacionVacaciones(a, q);
    }

    private static SortedSet<LocalDate> conjunto(List<LocalDate> fechas, String nombre) {
        if (fechas == null) {
            return new TreeSet<>();
        }
        if (fechas.size() > MAX_FECHAS) {
            throw new PeticionInvalidaException("La lista «" + nombre + "» tiene más de " + MAX_FECHAS + " días.");
        }
        // Sin contains(null): las listas inmutables lanzan NullPointerException.
        if (fechas.stream().anyMatch(Objects::isNull)) {
            throw new PeticionInvalidaException("La lista «" + nombre + "» contiene una fecha vacía.");
        }
        if (new HashSet<>(fechas).size() != fechas.size()) {
            throw new PeticionInvalidaException("La lista «" + nombre + "» repite algún día.");
        }
        return new TreeSet<>(fechas);
    }

    /** Todas las fechas de la petición, para leer de una vez lo que hay en esos días. */
    public SortedSet<LocalDate> fechas() {
        SortedSet<LocalDate> todas = new TreeSet<>(anadir);
        todas.addAll(quitar);
        return todas;
    }
}
