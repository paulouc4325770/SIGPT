package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.time.LocalDate;

/**
 * Datos de la solicitud de cotizacion a proveedores (RFQ).
 */
public class SolicitudCotizacionFormulario {

    private Integer idOrdenProduccion;
    private LocalDate fechaEmision;
    private String observaciones;

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

}