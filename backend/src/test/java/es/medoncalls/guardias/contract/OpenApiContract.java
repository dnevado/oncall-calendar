package es.medoncalls.guardias.contract;

import java.nio.file.Path;

import org.springframework.test.web.servlet.ResultMatcher;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;

/**
 * Valida peticiones y respuestas de MockMvc contra el contrato publicado
 * `contracts/guardias-cambios.openapi.yaml` (principio IV).
 */
public final class OpenApiContract {

    private static final String CONTRATO =
            Path.of("..", "contracts", "guardias-cambios.openapi.yaml").toAbsolutePath().normalize().toUri().toString();

    private static final OpenApiInteractionValidator VALIDADOR =
            // allOf (ReglasIncumplidas = Problem + infracciones) se fusiona antes de validar.
            OpenApiInteractionValidator.createForSpecificationUrl(CONTRATO).withResolveCombinators(true).build();

    private OpenApiContract() {
    }

    /** La petición y la respuesta cumplen el contrato. */
    public static ResultMatcher cumple() {
        return OpenApiValidationMatchers.openApi().isValid(VALIDADOR);
    }
}
