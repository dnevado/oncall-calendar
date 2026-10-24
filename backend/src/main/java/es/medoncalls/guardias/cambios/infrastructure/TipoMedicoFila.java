package es.medoncalls.guardias.cambios.infrastructure;

import es.medoncalls.guardias.cambios.domain.TipoMedico;

/** Registro de `medicos_tipo_historico` vigente en una fecha. */
public record TipoMedicoFila(String tipo, String subTipo) {

    public TipoMedico aTipoMedico() {
        return TipoMedico.desdeLegacy(tipo, subTipo);
    }
}
