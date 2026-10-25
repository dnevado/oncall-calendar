package es.medoncalls.guardias.contract;

import java.nio.file.Path;

import org.springframework.test.web.servlet.ResultMatcher;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;

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

    /** Para peticiones inválidas a propósito: solo se comprueba la respuesta. */
    private static final OpenApiInteractionValidator VALIDADOR_RESPUESTA =
            OpenApiInteractionValidator.createForSpecificationUrl(CONTRATO).withResolveCombinators(true)
                    .withLevelResolver(LevelResolver.create()
                            .withLevel("validation.request", ValidationReport.Level.IGNORE).build())
                    .build();

    private OpenApiContract() {
    }

    /** La petición y la respuesta cumplen el contrato. */
    public static ResultMatcher cumple() {
        return OpenApiValidationMatchers.openApi().isValid(VALIDADOR);
    }

    /** La respuesta cumple el contrato; la petición es inválida a propósito. */
    public static ResultMatcher respuestaCumple() {
        return OpenApiValidationMatchers.openApi().isValid(VALIDADOR_RESPUESTA);
    }
}
