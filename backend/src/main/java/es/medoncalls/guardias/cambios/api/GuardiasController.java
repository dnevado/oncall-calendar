package es.medoncalls.guardias.cambios.api;

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
import es.medoncalls.guardias.cambios.api.dto.GuardiaDto;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaMapper;
import es.medoncalls.guardias.common.PeticionInvalidaException;
import es.medoncalls.guardias.common.ServicioAccess;

/** Calendario de guardias del servicio. */
@RestController
@RequestMapping("/api/v1/servicios/{servicioId}/guardias")
public class GuardiasController {

    private final ServicioAccess servicioAccess;
    private final GuardiaMapper guardias;

    public GuardiasController(ServicioAccess servicioAccess, GuardiaMapper guardias) {
        this.servicioAccess = servicioAccess;
        this.guardias = guardias;
    }

    @GetMapping
    public List<GuardiaDto> listar(@AuthenticationPrincipal UsuarioSesion usuario,
            @PathVariable long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        if (hasta.isBefore(desde)) {
            throw new PeticionInvalidaException("La fecha «hasta» es anterior a «desde».");
        }
        return guardias.calendario(servicioId, desde, hasta).stream().map(GuardiaDto::de).toList();
    }
}
