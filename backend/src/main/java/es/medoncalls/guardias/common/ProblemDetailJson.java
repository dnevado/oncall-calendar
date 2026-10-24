package es.medoncalls.guardias.common;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Serializa los Problem Details sin `instance`, que Spring rellena con la ruta de la petición y
 * el contrato publicado no declara (`Problem`: type, title, status, detail).
 */
@Configuration
public class ProblemDetailJson {

    @JsonIgnoreProperties("instance")
    interface SinInstance extends ProblemDetailJacksonMixin {
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer problemDetailSinInstance() {
        return builder -> builder.mixIn(ProblemDetail.class, SinInstance.class);
    }
}
