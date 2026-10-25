package es.medoncalls.guardias.common;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.medoncalls.guardias.auth.CredencialesInvalidasException;
import es.medoncalls.guardias.cambios.application.ReglasIncumplidasException;
import es.medoncalls.guardias.cambios.domain.Infraccion;

/**
 * Errores de la API como Problem Details (RFC 7807). Las excepciones estándar de Spring MVC
 * (cuerpo ilegible, parámetros ausentes o mal tipados, validación) salen como 400 desde la clase
 * base. El 401 lo emite la configuración de seguridad.
 */
@RestControllerAdvice
public class ProblemDetailsHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ReglasIncumplidasException.class)
    ProblemDetail reglasIncumplidas(ReglasIncumplidasException ex) {
        ProblemDetail problema = problema(HttpStatus.UNPROCESSABLE_ENTITY, "Reglas incumplidas", ex.getMessage());
        List<InfraccionDto> infracciones = ex.infracciones().stream().map(InfraccionDto::de).toList();
        problema.setProperty("infracciones", infracciones);
        return problema;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(PeticionInvalidaException.class)
    ProblemDetail peticionInvalida(PeticionInvalidaException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición inválida", ex.getMessage());
    }

    @ExceptionHandler(ProhibidoException.class)
    ProblemDetail prohibido(ProhibidoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Prohibido", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    ProblemDetail conflicto(ConflictoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    /** Cualquier otro error: se registra y se responde sin detalles internos. */
    @ExceptionHandler(Exception.class)
    ProblemDetail errorInterno(Exception ex) {
        logger.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "No se ha podido completar la operación. No se ha aplicado ningún cambio.");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record InfraccionDto(String regla, String mensaje, Long medicoId, LocalDate fecha) {
        static InfraccionDto de(Infraccion infraccion) {
            return new InfraccionDto(infraccion.regla().codigo(), infraccion.mensaje(),
                    infraccion.medicoId(), infraccion.fecha());
        }
    }
}
