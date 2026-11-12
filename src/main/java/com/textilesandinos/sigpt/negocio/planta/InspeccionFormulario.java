package com.textilesandinos.sigpt.negocio.planta;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de inspeccion de calidad de un lote.
 */
public class InspeccionFormulario {

    private Integer idLote;
    private BigDecimal varianzaColorPct;
    private String resultado;
    private String observacion;
    private LocalDate fecha;

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public BigDecimal getVarianzaColorPct() {
        return varianzaColorPct;
    }

    public void setVarianzaColorPct(BigDecimal varianzaColorPct) {
        this.varianzaColorPct = varianzaColorPct;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

}
