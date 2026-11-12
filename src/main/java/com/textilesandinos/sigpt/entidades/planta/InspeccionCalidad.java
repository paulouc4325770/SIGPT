package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inspeccion de calidad del lote. Contrasta la varianza de color con la tolerancia de la ficha tecnica (RD7).
 *
 * <p>Clase del dominio que refleja la tabla {@code inspeccion_calidad}.
 */
public class InspeccionCalidad {

    private Integer idInspeccion;

    private Integer idLote;

    private Integer idAvance;

    private LocalDate fecha;

    private BigDecimal varianzaColorPct = new BigDecimal("0.00");

    /** Tolerancia tomada de la ficha tecnica (RD7) */
    private BigDecimal toleranciaAplicada = new BigDecimal("2.00");

    /** CONFORME, NO_CONFORME */
    private String resultado;

    private String observacion;

    public InspeccionCalidad() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdInspeccion() {
        return idInspeccion;
    }

    public void setIdInspeccion(Integer idInspeccion) {
        this.idInspeccion = idInspeccion;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getVarianzaColorPct() {
        return varianzaColorPct;
    }

    public void setVarianzaColorPct(BigDecimal varianzaColorPct) {
        this.varianzaColorPct = varianzaColorPct;
    }

    public BigDecimal getToleranciaAplicada() {
        return toleranciaAplicada;
    }

    public void setToleranciaAplicada(BigDecimal toleranciaAplicada) {
        this.toleranciaAplicada = toleranciaAplicada;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
