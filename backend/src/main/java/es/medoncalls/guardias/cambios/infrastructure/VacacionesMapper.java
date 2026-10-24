package es.medoncalls.guardias.cambios.infrastructure;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Vacaciones de los médicos: comunes a todos sus servicios (FR-011). */
@Mapper
public interface VacacionesMapper {

    List<VacacionFila> selectDias(@Param("medicoIds") Collection<Long> medicoIds,
            @Param("dias") Collection<String> dias);
}
