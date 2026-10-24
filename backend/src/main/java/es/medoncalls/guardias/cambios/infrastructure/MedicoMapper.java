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

    /**
     * Bloquea la fila del médico hasta el final de la transacción. Serializa los cambios que dan
     * guardias a un mismo médico, para que CG-013 vea las de días adyacentes (R9).
     */
    Long lockById(@Param("id") long id);

    List<MedicoFila> selectByIds(@Param("ids") Collection<Long> ids);

    /** Pertenecer al servicio = existe la fila; no se mira `ActivoServicio` (CG-015). */
    List<Long> selectMiembros(@Param("servicioId") long servicioId, @Param("ids") Collection<Long> ids);

    List<HistoricoTipoFila> selectHistoricoTipo(@Param("ids") Collection<Long> ids);

    /** Médicos con `Activo = 1` que pertenecen al servicio (FR-017). */
    List<MedicoFila> selectActivosDelServicio(@Param("servicioId") long servicioId);
}
