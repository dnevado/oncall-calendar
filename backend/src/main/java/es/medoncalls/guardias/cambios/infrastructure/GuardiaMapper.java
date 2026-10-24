package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;

/** Acceso a `guardias_medicos`. Los médicos se enlazan por `medicos.ID` (data-model). */
@Mapper
public interface GuardiaMapper {

    List<GuardiaFila> selectByServicioYRango(@Param("servicioId") long servicioId,
            @Param("desde") String desde, @Param("hasta") String hasta);

    GuardiaFila selectById(@Param("id") long id);

    List<GuardiaFila> selectByMedicosFechasServicio(@Param("medicoIds") Collection<Long> medicoIds,
            @Param("fechas") Collection<String> fechas, @Param("servicioId") long servicioId);

    List<GuardiaFila> selectByServicioYFecha(@Param("servicioId") long servicioId,
            @Param("fecha") String fecha);

    /**
     * Bloquea la fila del servicio hasta el final de la transacción: los cambios de un mismo
     * servicio se aplican de uno en uno (R9).
     */
    Long lockServicio(@Param("servicioId") long servicioId);

    /** Bloquea las guardias en orden de `Id` para evitar interbloqueos (R9). */
    List<GuardiaFila> lockByIds(@Param("ids") List<Long> ids);

    int updateMedico(@Param("guardiaId") long guardiaId, @Param("medicoId") long medicoId);

    int updateTipo(@Param("guardiaId") long guardiaId, @Param("tipo") String tipo);

    default List<GuardiaFila> calendario(long servicioId, LocalDate desde, LocalDate hasta) {
        return selectByServicioYRango(servicioId, FechaLegacy.texto(desde), FechaLegacy.texto(hasta));
    }

    default Optional<Guardia> buscar(long id) {
        return Optional.ofNullable(selectById(id)).map(GuardiaFila::aGuardia);
    }

    default List<Guardia> deMedicosEnFechas(Collection<Long> medicoIds, Collection<LocalDate> fechas,
            long servicioId) {
        return selectByMedicosFechasServicio(medicoIds, fechas.stream().map(FechaLegacy::texto).toList(), servicioId)
                .stream().map(GuardiaFila::aGuardia).toList();
    }

    default List<Guardia> delServicioEnFecha(long servicioId, LocalDate fecha) {
        return selectByServicioYFecha(servicioId, FechaLegacy.texto(fecha)).stream()
                .map(GuardiaFila::aGuardia).toList();
    }

    default List<Guardia> bloquear(List<Long> ids) {
        return lockByIds(ids.stream().sorted().distinct().toList()).stream()
                .map(GuardiaFila::aGuardia).toList();
    }

    default void cambiarTipo(long guardiaId, TipoGuardia tipo) {
        updateTipo(guardiaId, tipo.valorLegacy());
    }
}
