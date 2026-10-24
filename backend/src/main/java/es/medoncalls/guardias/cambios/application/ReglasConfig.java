package es.medoncalls.guardias.cambios.application;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.medoncalls.guardias.cambios.domain.AjusteResidentes;
import es.medoncalls.guardias.cambios.domain.Mensajes;
import es.medoncalls.guardias.cambios.domain.ReglasCambio;

/** Reglas y ajuste R3–R5 son dominio puro; aquí se les dan los textos de `messages_es.properties`. */
@Configuration
public class ReglasConfig {

    private static final Locale ES = Locale.forLanguageTag("es");

    @Bean
    Mensajes mensajes(MessageSource fuente) {
        return (clave, argumentos) -> fuente.getMessage(clave, argumentos, ES);
    }

    @Bean
    ReglasCambio reglasCambio(Mensajes mensajes) {
        return new ReglasCambio(mensajes);
    }

    @Bean
    AjusteResidentes ajusteResidentes() {
        return new AjusteResidentes();
    }
}
