package es.medoncalls.guardias.vacaciones.infrastructure;

import java.util.Collection;
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

    /** Días de la lista que ya son de vacaciones del médico, como texto `yyyy-MM-dd`. */
    List<String> diasExistentes(@Param("medicoId") long medicoId, @Param("dias") Collection<String> dias);

    /**
     * Añade los días; si otro proceso (p. ej. el legacy) ha insertado el mismo día a la vez, no falla
     * (`IND_VACACIONES_01`, research V3).
     */
    int insertarDias(@Param("medicoId") long medicoId, @Param("dias") Collection<String> dias);

    /** Quita los días por clave (`IdMedico`, `FDay`); el resto de vacaciones del médico no cambia (VAC-007). */
    int borrarDias(@Param("medicoId") long medicoId, @Param("dias") Collection<String> dias);
}
