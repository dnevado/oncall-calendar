package es.medoncalls.guardias.cambios.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Contrato: `Candidato`. `guardiaDestinoId` solo en un CAMBIO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CandidatoDto(MedicoResumenDto medico, Long guardiaDestinoId) {
}
