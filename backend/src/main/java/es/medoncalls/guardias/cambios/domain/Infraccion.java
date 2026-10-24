package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;

/**
 * Regla incumplida por un cambio, con un texto comprensible que nombra al médico y la fecha
 * afectados cuando aplican (FR-016, SC-003).
 */
public record Infraccion(ReglaCodigo regla, String mensaje, Long medicoId, LocalDate fecha) {
}
