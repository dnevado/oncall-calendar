package es.medoncalls.guardias.cambios.domain;

import java.util.Locale;
import java.util.Set;

/**
 * Tipo de un médico en una fecha (R11). El subtipo solo tiene significado para residentes:
 * en adjuntos es ruido de datos legacy (discovery D13) y se ignora.
 */
public record TipoMedico(Categoria categoria, String subtipo) {

    public enum Categoria { ADJUNTO, RESIDENTE }

    private static final Set<String> SENIOR = Set.of("R3", "R4", "R5");

    public static TipoMedico desdeLegacy(String tipo, String subtipo) {
        Categoria categoria = Categoria.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        String sub = subtipo == null ? null : subtipo.trim().toUpperCase(Locale.ROOT);
        return new TipoMedico(categoria, categoria == Categoria.RESIDENTE ? sub : null);
    }

    /** Residente R3, R4 o R5 (FR-020). */
    public boolean esR3aR5() {
        return categoria == Categoria.RESIDENTE && subtipo != null && SENIOR.contains(subtipo);
    }

    /** Residente simulado (research R11). No tiene exenciones en esta funcionalidad (FR-012). */
    public boolean esSimulado() {
        return categoria == Categoria.RESIDENTE && "SIMULADO".equals(subtipo);
    }

    public boolean mismaCategoria(TipoMedico otro) {
        return otro != null && categoria == otro.categoria;
    }
}
