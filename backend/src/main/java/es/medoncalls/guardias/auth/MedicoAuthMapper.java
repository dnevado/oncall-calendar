package es.medoncalls.guardias.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Login sobre `medicos` y rol de administrador en `guardias_servicios_medicos`. */
@Mapper
public interface MedicoAuthMapper {

    MedicoAuthFila selectPorEmail(@Param("email") String email);

    MedicoAuthFila selectPorId(@Param("id") long id);

    int updateHashV2(@Param("id") long id, @Param("hash") String hash, @Param("origen") String origen);

    /**
     * Suma un fallo y bloquea al llegar al máximo. Si el bloqueo anterior ya venció, el contador
     * empieza de nuevo (FR-029).
     */
    int registrarFallo(@Param("id") long id, @Param("ahora") LocalDateTime ahora,
            @Param("maxIntentos") int maxIntentos, @Param("bloqueadoHasta") LocalDateTime bloqueadoHasta);

    int reiniciarFallos(@Param("id") long id);

    List<ServicioResumen> selectServiciosAdministrados(@Param("medicoId") long medicoId);

    boolean esAdministrador(@Param("medicoId") long medicoId, @Param("servicioId") long servicioId);
}
