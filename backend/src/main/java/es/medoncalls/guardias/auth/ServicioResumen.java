package es.medoncalls.guardias.auth;

import java.io.Serializable;

/** Servicio que administra el usuario. */
public record ServicioResumen(Long id, String nombre) implements Serializable {
}
