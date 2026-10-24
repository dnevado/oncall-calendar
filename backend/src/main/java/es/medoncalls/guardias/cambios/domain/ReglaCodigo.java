package es.medoncalls.guardias.cambios.domain;

/** Códigos de regla del catálogo (docs/rules) tal como los publica el contrato. */
public enum ReglaCodigo {
    CG_005("CG-005"),
    CG_006("CG-006"),
    CG_008("CG-008"),
    CG_009("CG-009"),
    CG_011("CG-011"),
    CG_012("CG-012"),
    CG_013("CG-013"),
    CG_014("CG-014"),
    CG_015("CG-015"),
    MISMO_MEDICO("MISMO_MEDICO");

    private final String codigo;

    ReglaCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
