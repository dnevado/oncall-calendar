package es.medoncalls.guardias.vacaciones.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.medoncalls.guardias.auth.UsuarioSesion;
import es.medoncalls.guardias.common.ServicioAccess;
import es.medoncalls.guardias.vacaciones.api.dto.ModificarVacacionesRequestDto;
import es.medoncalls.guardias.vacaciones.api.dto.ResultadoVacacionesDto;
import es.medoncalls.guardias.vacaciones.api.dto.VacacionesMedicoDto;
import es.medoncalls.guardias.vacaciones.application.ConsultarVacacionesService;
import es.medoncalls.guardias.vacaciones.application.ModificarVacacionesService;

/** Vacaciones de los médicos del servicio (002). Solo para el administrador del servicio (FR-001). */
@RestController
@RequestMapping("/api/v1/servicios/{servicioId}")
public class VacacionesController {

    private final ServicioAccess servicioAccess;
    private final ConsultarVacacionesService consulta;
    private final ModificarVacacionesService modificacion;

    public VacacionesController(ServicioAccess servicioAccess, ConsultarVacacionesService consulta,
            ModificarVacacionesService modificacion) {
        this.servicioAccess = servicioAccess;
        this.consulta = consulta;
        this.modificacion = modificacion;
    }

    @GetMapping("/vacaciones")
    public List<VacacionesMedicoDto> listar(@AuthenticationPrincipal UsuarioSesion usuario,
            @PathVariable long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long medicoId) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        return consulta.consultar(servicioId, desde, hasta, medicoId).stream().map(VacacionesMedicoDto::de).toList();
    }

    /**
     * Añade y quita días (FR-003 a FR-011). Como en la 001, un cuerpo ilegible da 400 antes de llegar
     * aquí; con un cuerpo válido, el rol se comprueba siempre primero.
     */
    @PatchMapping("/medicos/{medicoId}/vacaciones")
    public ResultadoVacacionesDto modificar(@AuthenticationPrincipal UsuarioSesion usuario,
            @PathVariable long servicioId, @PathVariable long medicoId,
            @RequestBody ModificarVacacionesRequestDto peticion) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        return ResultadoVacacionesDto.de(modificacion.modificar(servicioId, medicoId, peticion.anadir(),
                peticion.quitar()));
    }
}
