package es.medoncalls.guardias.cambios.api.dto;

import es.medoncalls.guardias.cambios.domain.TipoCambio;
import jakarta.validation.constraints.NotNull;

/** Contrato: `RegistrarCambioRequest`. El administrador sale de la sesión, nunca del cuerpo. */
public record RegistrarCambioRequest(
        @NotNull TipoCambio tipo,
        @NotNull Long guardiaOrigenId,
        @NotNull Long cedenteId,
        @NotNull Long receptorId,
        Long guardiaDestinoId) {
}
