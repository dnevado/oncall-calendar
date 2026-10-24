package es.medoncalls.guardias.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Vectores calculados fuera de la aplicación con el algoritmo legacy: SHA-512 de la contraseña en
 * UTF-8 con sal vacía, codificado en Base64 (`src/guardias/security/SecurityUtil.java:71-100`).
 * Las contraseñas son ficticias.
 */
class LegacyPasswordVerifierTest {

    private static final String HASH_SECRETO =
            "sPIMQLUWvnS8tb/Cn1SIf62onpEAoabmtxGPYdmV9T3rS+vOyK+PeQ8J2oyxdOqcEnMle+ivE0PawIJUlLrUiA==";
    private static final String HASH_CON_ENE =
            "0qR0ZZyg0p1azqZ9M729DYdPE345TWPUohLcQDVzmWIaw17xx8Jaq0tPfaXqzM7Sb98AJUbJPXacEA8RewNh4Q==";

    private final LegacyPasswordVerifier verifier = new LegacyPasswordVerifier();

    @Test
    void calculaElMismoHashQueElLegacy() {
        assertThat(verifier.hashLegacy("secreto")).isEqualTo(HASH_SECRETO);
        assertThat(verifier.hashLegacy("Contraseña-Ficticia-1")).isEqualTo(HASH_CON_ENE);
    }

    @Test
    void aceptaLaContraseñaCorrecta() {
        assertThat(verifier.verifica("secreto", HASH_SECRETO)).isTrue();
        assertThat(verifier.verifica("Contraseña-Ficticia-1", HASH_CON_ENE)).isTrue();
    }

    @Test
    void rechazaContraseñaIncorrectaOHashAusente() {
        assertThat(verifier.verifica("Secreto", HASH_SECRETO)).isFalse();
        assertThat(verifier.verifica("secreto", null)).isFalse();
        assertThat(verifier.verifica("secreto", "")).isFalse();
        assertThat(verifier.verifica(null, HASH_SECRETO)).isFalse();
    }
}
