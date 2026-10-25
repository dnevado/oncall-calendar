package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Escritura y lectura de `guardias_cambios`. El sistema nuevo solo inserta: un cambio no se
 * modifica ni se borra; se corrige con el inverso (FR-028).
 */
@Mapper
public interface CambioMapper {

    int insert(NuevoCambio cambio);

    CambioFila selectById(@Param("id") long id);

    CambioFila selectByIdempotencyKey(@Param("clave") String clave);

    List<CambioFila> selectByServicioYRango(@Param("servicioId") long servicioId, @Param("desde") String desde,
            @Param("hasta") String hasta);

    /** Cambios registrados por la aplicación cuya fecha origen o destino está en el rango. */
    default List<CambioFila> delServicio(long servicioId, LocalDate desde, LocalDate hasta) {
        return selectByServicioYRango(servicioId, FechaLegacy.texto(desde), FechaLegacy.texto(hasta));
    }
}
