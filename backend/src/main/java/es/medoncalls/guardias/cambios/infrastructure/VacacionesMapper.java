package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Vacaciones de un médico: comunes a todos sus servicios (FR-011). */
@Mapper
public interface VacacionesMapper {

    boolean existe(@Param("medicoId") long medicoId, @Param("dia") String dia);

    default boolean tieneVacaciones(long medicoId, LocalDate dia) {
        return existe(medicoId, FechaLegacy.texto(dia));
    }
}
