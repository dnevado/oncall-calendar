package es.medoncalls.guardias.common;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj del servicio: toda comparación con «hoy» usa esta zona horaria (FR-008, FR-009). */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${guardias.zona-horaria}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}
