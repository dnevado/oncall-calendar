package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import es.medoncalls.guardias.cambios.domain.TipoMedico;

/** Lectura de médicos, pertenencia a servicio y tipo en una fecha. */
@Mapper
public interface MedicoMapper {

    MedicoFila selectById(@Param("id") long id);

    /** Pertenecer al servicio = existe la fila; no se mira `ActivoServicio` (CG-015). */
    boolean perteneceAServicio(@Param("medicoId") long medicoId, @Param("servicioId") long servicioId);

    TipoMedicoFila selectTipoEnFecha(@Param("medicoId") long medicoId, @Param("fecha") LocalDate fecha);

    /** Médicos con `Activo = 1` que pertenecen al servicio (FR-017). */
    List<MedicoFila> selectActivosDelServicio(@Param("servicioId") long servicioId);

    default Optional<MedicoFila> buscar(long id) {
        return Optional.ofNullable(selectById(id));
    }

    default Optional<TipoMedico> tipoEnFecha(long medicoId, LocalDate fecha) {
        return Optional.ofNullable(selectTipoEnFecha(medicoId, fecha)).map(TipoMedicoFila::aTipoMedico);
    }
}
