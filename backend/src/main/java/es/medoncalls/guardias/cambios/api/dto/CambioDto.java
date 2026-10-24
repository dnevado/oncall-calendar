package es.medoncalls.guardias.cambios.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.medoncalls.guardias.cambios.domain.TipoCambio;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.cambios.infrastructure.CambioFila;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.cambios.infrastructure.NombreMedico;

/** Contrato: `Cambio`. `fechaDestino` y `guardiaDestino` solo en un CAMBIO. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CambioDto(
        long id,
        TipoCambio tipo,
        LocalDate fechaOrigen,
        LocalDate fechaDestino,
        MedicoResumenDto cedente,
        MedicoResumenDto receptor,
        GuardiaAntesDto guardiaOrigen,
        GuardiaAntesDto guardiaDestino,
        MedicoResumenDto registradoPor,
        OffsetDateTime registradoEn) {

    /** Tipo y festivo de la guardia antes del cambio (FR-003, FR-023). */
    public record GuardiaAntesDto(TipoGuardia tipo, boolean festivo) {

        static GuardiaAntesDto de(String tipoLegacy, Integer festivo) {
            return new GuardiaAntesDto(TipoGuardia.desdeLegacy(tipoLegacy), festivo != null && festivo != 0);
        }
    }

    public static CambioDto de(CambioFila fila, ZoneId zona) {
        TipoCambio tipo = TipoCambio.valueOf(fila.tipoCambio());
        boolean esCambio = tipo == TipoCambio.CAMBIO;
        return new CambioDto(
                fila.id(),
                tipo,
                FechaLegacy.fecha(fila.fechaIniCambio()),
                esCambio ? FechaLegacy.fecha(fila.fechaFinCambio()) : null,
                new MedicoResumenDto(fila.cedenteId(), NombreMedico.de(fila.cedenteNombre(), fila.cedenteApellidos())),
                new MedicoResumenDto(fila.receptorId(),
                        NombreMedico.de(fila.receptorNombre(), fila.receptorApellidos())),
                GuardiaAntesDto.de(fila.tipoGuardiaOrigen(), fila.festivoOrigen()),
                esCambio ? GuardiaAntesDto.de(fila.tipoGuardiaDestino(), fila.festivoDestino()) : null,
                new MedicoResumenDto(fila.registradoPorId(),
                        NombreMedico.de(fila.registradoPorNombre(), fila.registradoPorApellidos())),
                fila.fechaHoraRegistro().atZone(zona).toOffsetDateTime());
    }
}
