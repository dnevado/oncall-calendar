package es.medoncalls.guardias.cambios.domain;

import java.util.Locale;

/**
 * Tipo de guardia. En el esquema legacy se guarda en minúsculas y la guardia de residente
 * es la cadena vacía (data-model, «Guardia»).
 */
public enum TipoGuardia {
    PRESENCIA("presencia"),
    LOCALIZADA("localizada"),
    REFUERZO("refuerzo"),
    RESIDENTE("");

    private final String valorLegacy;

    TipoGuardia(String valorLegacy) {
        this.valorLegacy = valorLegacy;
    }

    public String valorLegacy() {
        return valorLegacy;
    }

    public static TipoGuardia desdeLegacy(String valor) {
        if (valor == null || valor.isBlank()) {
            return RESIDENTE;
        }
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        for (TipoGuardia tipo : values()) {
            if (tipo.valorLegacy.equals(normalizado)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de guardia legacy desconocido: " + valor);
    }
}
