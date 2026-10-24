package es.medoncalls.guardias.cambios.infrastructure;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Lectura de médicos, pertenencia a servicio e histórico de tipo. Las lecturas son por lotes de
 * médicos para que el cálculo de candidatos no dependa del tamaño de la plantilla (FR-017).
 */
@Mapper
public interface MedicoMapper {

    List<MedicoFila> selectByIds(@Param("ids") Collection<Long> ids);

    /** Pertenecer al servicio = existe la fila; no se mira `ActivoServicio` (CG-015). */
    List<Long> selectMiembros(@Param("servicioId") long servicioId, @Param("ids") Collection<Long> ids);

    List<HistoricoTipoFila> selectHistoricoTipo(@Param("ids") Collection<Long> ids);

    /** Médicos con `Activo = 1` que pertenecen al servicio (FR-017). */
    List<MedicoFila> selectActivosDelServicio(@Param("servicioId") long servicioId);
}
