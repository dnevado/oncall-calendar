package es.medoncalls.guardias.auth;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.medoncalls.guardias.cambios.infrastructure.NombreMedico;

/**
 * Login con las credenciales del legacy (R2). La primera vez que una contraseña legacy se
 * verifica se guarda su hash Argon2id junto a la huella del hash legacy; si el legacy cambia la
 * contraseña, la huella deja de coincidir y se vuelve a verificar contra el legacy.
 */
@Service
public class AuthService {

    private final MedicoAuthMapper mapper;
    private final LegacyPasswordVerifier legacy;
    private final Argon2PasswordEncoder argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private final Clock clock;
    private final int maxIntentos;
    private final int bloqueoMinutos;

    public AuthService(MedicoAuthMapper mapper, LegacyPasswordVerifier legacy, Clock clock,
            @Value("${guardias.login.max-intentos}") int maxIntentos,
            @Value("${guardias.login.bloqueo-minutos}") int bloqueoMinutos) {
        this.mapper = mapper;
        this.legacy = legacy;
        this.clock = clock;
        this.maxIntentos = maxIntentos;
        this.bloqueoMinutos = bloqueoMinutos;
    }

    /** Devuelve el usuario autenticado o lanza {@link CredencialesInvalidasException}. */
    @Transactional(noRollbackFor = CredencialesInvalidasException.class)
    public UsuarioSesion login(String email, String password) {
        MedicoAuthFila medico = mapper.selectPorEmail(email);
        if (medico == null) {
            legacy.hashLegacy(password == null ? "" : password); // coste similar al de una cuenta real
            throw new CredencialesInvalidasException();
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (medico.bloqueadoEn(ahora)) {
            throw new CredencialesInvalidasException();
        }
        if (!medico.puedeEntrar() || !passwordCorrecta(medico, password)) {
            mapper.registrarFallo(medico.id(), ahora, maxIntentos, ahora.plusMinutes(bloqueoMinutos));
            throw new CredencialesInvalidasException();
        }
        mapper.reiniciarFallos(medico.id());
        return new UsuarioSesion(medico.id(), NombreMedico.de(medico.nombre(), medico.apellidos()));
    }

    private boolean passwordCorrecta(MedicoAuthFila medico, String password) {
        if (medico.password() == null || password == null) {
            return false;
        }
        String huellaActual = legacy.huella(medico.password());
        if (medico.passwordHashV2() != null && huellaActual.equals(medico.passwordHashV2Origen())) {
            return argon2.matches(password, medico.passwordHashV2());
        }
        if (!legacy.verifica(password, medico.password())) {
            return false;
        }
        mapper.updateHashV2(medico.id(), argon2.encode(password), huellaActual);
        return true;
    }
}
