package es.medoncalls.guardias.vacaciones.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.medoncalls.guardias.auth.UsuarioSesion;
import es.medoncalls.guardias.common.ServicioAccess;
import es.medoncalls.guardias.vacaciones.api.dto.VacacionesMedicoDto;
import es.medoncalls.guardias.vacaciones.application.ConsultarVacacionesService;

/** Vacaciones de los médicos del servicio (002). Solo para el administrador del servicio (FR-001). */
@RestController
@RequestMapping("/api/v1/servicios/{servicioId}")
public class VacacionesController {

    private final ServicioAccess servicioAccess;
    private final ConsultarVacacionesService consulta;

    public VacacionesController(ServicioAccess servicioAccess, ConsultarVacacionesService consulta) {
        this.servicioAccess = servicioAccess;
        this.consulta = consulta;
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
}
