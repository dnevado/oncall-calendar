package es.medoncalls.guardias.common;

import org.springframework.stereotype.Component;

import es.medoncalls.guardias.auth.MedicoAuthMapper;
import es.medoncalls.guardias.auth.UsuarioSesion;

/**
 * Guarda de todas las operaciones `/servicios/{servicioId}/**`: solo el administrador de ese
 * servicio. Se consulta la base en cada petición para que una revocación surta efecto en el acto
 * (CG-002, FR-001).
 */
@Component
public class ServicioAccess {

    private final MedicoAuthMapper mapper;

    public ServicioAccess(MedicoAuthMapper mapper) {
        this.mapper = mapper;
    }

    public void exigirAdministrador(UsuarioSesion usuario, long servicioId) {
        if (usuario == null || !mapper.esAdministrador(usuario.medicoId(), servicioId)) {
            throw new ProhibidoException();
        }
    }
}
