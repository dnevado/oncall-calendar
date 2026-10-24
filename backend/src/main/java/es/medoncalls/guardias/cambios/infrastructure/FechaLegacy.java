package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;

/** El esquema legacy guarda las fechas como texto `yyyy-MM-dd` (data-model, «Guardia»). */
public final class FechaLegacy {

    private FechaLegacy() {
    }

    public static String texto(LocalDate fecha) {
        return fecha.toString();
    }

    public static LocalDate fecha(String texto) {
        return LocalDate.parse(texto.substring(0, 10));
    }
}
