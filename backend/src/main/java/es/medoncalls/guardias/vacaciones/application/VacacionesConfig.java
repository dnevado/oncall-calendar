package es.medoncalls.guardias.vacaciones.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.medoncalls.guardias.cambios.domain.Mensajes;
import es.medoncalls.guardias.vacaciones.domain.ReglasVacaciones;

/** Las reglas de vacaciones son dominio puro; reciben los textos de `messages_es.properties`. */
@Configuration
public class VacacionesConfig {

    @Bean
    ReglasVacaciones reglasVacaciones(Mensajes mensajes) {
        return new ReglasVacaciones(mensajes);
    }
}
