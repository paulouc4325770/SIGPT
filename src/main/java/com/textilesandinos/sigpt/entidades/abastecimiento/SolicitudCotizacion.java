package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.time.LocalDate;

/**
 * Solicitud de Cotizacion (RFQ) enviada a los proveedores homologados.
 *
 * <p>Clase del dominio que refleja la tabla {@code solicitud_cotizacion}.
 */
public class SolicitudCotizacion {

    private Integer idSolicitud;

    private String codigo;

    private Integer idOrdenProduccion;

    private LocalDate fechaEmision;

    private LocalDate fechaCierre;

    private String estado = "EMITIDA";

    private String observaciones;

    public SolicitudCotizacion() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
