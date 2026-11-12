package com.textilesandinos.sigpt.negocio.planta;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de registro de avance del operario en el terminal.
 */
public class AvanceFormulario {

    private Integer idAsignacion;
    private LocalDate fecha;
    private BigDecimal metrosProducidos;
    private BigDecimal metrosMerma;
    private String observacion;
    private String tipoIncidencia;
    private String descripcionIncidencia;

    public Integer getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Integer idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMetrosProducidos() {
        return metrosProducidos;
    }

    public void setMetrosProducidos(BigDecimal metrosProducidos) {
        this.metrosProducidos = metrosProducidos;
    }

    public BigDecimal getMetrosMerma() {
        return metrosMerma;
    }

    public void setMetrosMerma(BigDecimal metrosMerma) {
        this.metrosMerma = metrosMerma;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(String tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public String getDescripcionIncidencia() {
        return descripcionIncidencia;
    }

    public void setDescripcionIncidencia(String descripcionIncidencia) {
        this.descripcionIncidencia = descripcionIncidencia;
    }

}
