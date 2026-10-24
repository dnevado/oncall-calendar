package es.medoncalls.guardias.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Reloj ajustable para fijar «hoy» y simular el paso del tiempo en las pruebas. */
public class RelojPrueba extends Clock {

    public static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private volatile Instant ahora;

    public RelojPrueba(LocalDate hoy) {
        fijar(hoy);
    }

    public void fijar(LocalDate hoy) {
        ahora = hoy.atTime(10, 0).atZone(ZONA).toInstant();
    }

    public void avanzar(Duration duracion) {
        ahora = ahora.plus(duracion);
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return Clock.fixed(ahora, zona);
    }

    @Override
    public Instant instant() {
        return ahora;
    }
}
