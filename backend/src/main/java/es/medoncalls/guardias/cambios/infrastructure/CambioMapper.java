package es.medoncalls.guardias.cambios.infrastructure;

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
}
