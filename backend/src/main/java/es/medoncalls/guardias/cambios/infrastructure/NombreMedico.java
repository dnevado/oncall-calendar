package es.medoncalls.guardias.cambios.infrastructure;

/** Nombre y apellidos tal como los muestra el legacy. */
public final class NombreMedico {

    private NombreMedico() {
    }

    public static String de(String nombre, String apellidos) {
        String n = nombre == null ? "" : nombre.trim();
        String a = apellidos == null ? "" : apellidos.trim();
        return (n + " " + a).trim();
    }
}
