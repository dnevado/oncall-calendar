package es.medoncalls.guardias.cambios.api;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.medoncalls.guardias.auth.UsuarioSesion;
import es.medoncalls.guardias.cambios.api.dto.CambioDto;
import es.medoncalls.guardias.cambios.api.dto.CandidatoDto;
import es.medoncalls.guardias.cambios.api.dto.MedicoResumenDto;
import es.medoncalls.guardias.cambios.api.dto.RegistrarCambioRequest;
import es.medoncalls.guardias.cambios.application.CandidatosService;
import es.medoncalls.guardias.cambios.application.RegistrarCambioService;
import es.medoncalls.guardias.cambios.domain.SolicitudCambio;
import es.medoncalls.guardias.cambios.domain.TipoCambio;
import es.medoncalls.guardias.cambios.infrastructure.CambioMapper;
import es.medoncalls.guardias.common.PeticionInvalidaException;
import es.medoncalls.guardias.common.ServicioAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/** Registro y listado de cambios, y ayuda para elegir receptor. Todo exige ser administrador del servicio. */
@RestController
@RequestMapping("/api/v1/servicios/{servicioId}")
public class CambiosController {

    private final ServicioAccess servicioAccess;
    private final RegistrarCambioService registrar;
    private final CandidatosService candidatos;
    private final CambioMapper cambios;
    private final Clock clock;

    public CambiosController(ServicioAccess servicioAccess, RegistrarCambioService registrar,
            CandidatosService candidatos, CambioMapper cambios, Clock clock) {
        this.servicioAccess = servicioAccess;
        this.registrar = registrar;
        this.candidatos = candidatos;
        this.cambios = cambios;
        this.clock = clock;
    }

    /** Cambios registrados por la aplicación, del más reciente al más antiguo (FR-028: base del inverso). */
    @GetMapping("/cambios")
    public List<CambioDto> listar(@AuthenticationPrincipal UsuarioSesion usuario, @PathVariable long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        if (hasta.isBefore(desde)) {
            throw new PeticionInvalidaException("La fecha «hasta» es anterior a «desde».");
        }
        return cambios.delServicio(servicioId, desde, hasta).stream()
                .map(c -> CambioDto.de(c, clock.getZone()))
                .toList();
    }

    @PostMapping("/cambios")
    @ResponseStatus(HttpStatus.CREATED)
    public CambioDto registrar(@AuthenticationPrincipal UsuarioSesion usuario, @PathVariable long servicioId,
            @RequestHeader(name = "Idempotency-Key", required = false) @Size(max = 64) String claveIdempotencia,
            @Valid @RequestBody RegistrarCambioRequest peticion) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        if (peticion.tipo() == TipoCambio.CESION && peticion.guardiaDestinoId() != null) {
            throw new PeticionInvalidaException("Una cesión no lleva guardia destino.");
        }
        if (peticion.tipo() == TipoCambio.CAMBIO && peticion.guardiaDestinoId() == null) {
            throw new PeticionInvalidaException("Un intercambio necesita la guardia destino.");
        }
        SolicitudCambio solicitud = new SolicitudCambio(peticion.tipo(), servicioId, peticion.guardiaOrigenId(),
                peticion.cedenteId(), peticion.receptorId(), peticion.guardiaDestinoId());
        return CambioDto.de(registrar.registrar(usuario.medicoId(), solicitud, claveIdempotencia), clock.getZone());
    }

    @GetMapping("/guardias/{guardiaId}/receptores-candidatos")
    public List<CandidatoDto> receptoresCandidatos(@AuthenticationPrincipal UsuarioSesion usuario,
            @PathVariable long servicioId, @PathVariable long guardiaId, @RequestParam TipoCambio tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDestino) {
        servicioAccess.exigirAdministrador(usuario, servicioId);
        List<CandidatosService.Candidato> encontrados;
        if (tipo == TipoCambio.CAMBIO) {
            if (fechaDestino == null) {
                throw new PeticionInvalidaException("Para un intercambio hay que indicar la fecha destino.");
            }
            encontrados = candidatos.paraIntercambio(servicioId, guardiaId, fechaDestino);
        } else {
            encontrados = candidatos.paraCesion(servicioId, guardiaId);
        }
        return encontrados.stream()
                .map(c -> new CandidatoDto(new MedicoResumenDto(c.medicoId(), c.nombre()), c.guardiaDestinoId()))
                .toList();
    }
}
