package es.medoncalls.guardias.vacaciones.infrastructure;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Vacaciones de los médicos en la tabla legacy `vacaciones_medicos` (research V4): fechas como
 * texto `yyyy-MM-dd` y un registro por médico y día (`IND_VACACIONES_01`).
 */
@Mapper
public interface VacacionMapper {

    /** El médico está dado de alta en el servicio, activo o no (002, Assumptions). */
    boolean perteneceAlServicio(@Param("servicioId") long servicioId, @Param("medicoId") long medicoId);

    /** Médicos del servicio (todos, o solo `medicoId`) por apellidos y nombre. */
    List<MedicoServicioFila> medicosDelServicio(@Param("servicioId") long servicioId,
            @Param("medicoId") Long medicoId);

    /** Días de vacaciones de los médicos del servicio en el rango, con su conflicto (research V5). */
    List<DiaVacacionFila> diasConConflicto(@Param("servicioId") long servicioId, @Param("desde") String desde,
            @Param("hasta") String hasta, @Param("medicoId") Long medicoId);
}
