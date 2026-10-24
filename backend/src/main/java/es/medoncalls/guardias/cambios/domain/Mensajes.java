package es.medoncalls.guardias.cambios.domain;

/** Textos para el usuario (`messages_es.properties`), resueltos fuera del dominio. */
@FunctionalInterface
public interface Mensajes {

    String texto(String clave, Object... argumentos);
}
