package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDateTime;

/**
 * Alta en `guardias_cambios` según la correspondencia del data-model. Mutable solo porque MyBatis
 * devuelve aquí el `IdCambio` generado.
 */
public class NuevoCambio {

    private Long id;
    private final String tipoCambio;
    private final String fechaIniCambio;
    private final String fechaFinCambio;
    private final long idMedicoSolicitante;
    private final long idMedicoDestino;
    private final String fechaRegistro;
    private final long usuarioAprobacion;
    private final long servicioId;
    private final String tipoGuardiaOrigen;
    private final int festivoOrigen;
    private final String tipoGuardiaDestino;
    private final Integer festivoDestino;
    private final LocalDateTime fechaHoraRegistro;
    private final String idempotencyKey;

    public NuevoCambio(String tipoCambio, String fechaIniCambio, String fechaFinCambio, long idMedicoSolicitante,
            long idMedicoDestino, String fechaRegistro, long usuarioAprobacion, long servicioId,
            String tipoGuardiaOrigen, int festivoOrigen, String tipoGuardiaDestino, Integer festivoDestino,
            LocalDateTime fechaHoraRegistro, String idempotencyKey) {
        this.tipoCambio = tipoCambio;
        this.fechaIniCambio = fechaIniCambio;
        this.fechaFinCambio = fechaFinCambio;
        this.idMedicoSolicitante = idMedicoSolicitante;
        this.idMedicoDestino = idMedicoDestino;
        this.fechaRegistro = fechaRegistro;
        this.usuarioAprobacion = usuarioAprobacion;
        this.servicioId = servicioId;
        this.tipoGuardiaOrigen = tipoGuardiaOrigen;
        this.festivoOrigen = festivoOrigen;
        this.tipoGuardiaDestino = tipoGuardiaDestino;
        this.festivoDestino = festivoDestino;
        this.fechaHoraRegistro = fechaHoraRegistro;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public String getFechaIniCambio() {
        return fechaIniCambio;
    }

    public String getFechaFinCambio() {
        return fechaFinCambio;
    }

    public long getIdMedicoSolicitante() {
        return idMedicoSolicitante;
    }

    public long getIdMedicoDestino() {
        return idMedicoDestino;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public long getUsuarioAprobacion() {
        return usuarioAprobacion;
    }

    public long getServicioId() {
        return servicioId;
    }

    public String getTipoGuardiaOrigen() {
        return tipoGuardiaOrigen;
    }

    public int getFestivoOrigen() {
        return festivoOrigen;
    }

    public String getTipoGuardiaDestino() {
        return tipoGuardiaDestino;
    }

    public Integer getFestivoDestino() {
        return festivoDestino;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
