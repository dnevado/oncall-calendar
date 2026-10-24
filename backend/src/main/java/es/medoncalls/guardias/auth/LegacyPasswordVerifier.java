package es.medoncalls.guardias.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Verifica contraseñas contra el hash del legacy: SHA-512 sin sal, en Base64
 * (`src/guardias/security/SecurityUtil.java:71-100`). Solo se usa para migrar al hash nuevo (R2).
 */
@Component
public class LegacyPasswordVerifier {

    public String hashLegacy(String plano) {
        return Base64.getEncoder().encodeToString(digest("SHA-512", plano));
    }

    /** Comparación en tiempo constante. */
    public boolean verifica(String plano, String hashLegacy) {
        if (plano == null || hashLegacy == null || hashLegacy.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(hashLegacy(plano).getBytes(StandardCharsets.US_ASCII),
                hashLegacy.getBytes(StandardCharsets.US_ASCII));
    }

    /** Huella del hash legacy guardada junto al hash nuevo para detectar cambios de contraseña. */
    public String huella(String hashLegacy) {
        return HexFormat.of().formatHex(digest("SHA-256", hashLegacy));
    }

    private static byte[] digest(String algoritmo, String texto) {
        try {
            return MessageDigest.getInstance(algoritmo).digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
