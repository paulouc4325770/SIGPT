package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;

/**
 * Resultado de la inspeccion de calidad de un lote (Regla R-Merma).
 */
public class ResultadoCalidad {

    private boolean conforme;
    private BigDecimal toleranciaAplicada = BigDecimal.ZERO;
    private BigDecimal porcentajeAvanceOrden = BigDecimal.ZERO;
    private boolean cierreHabilitado;
    private String mensaje;

    public boolean isConforme() {
        return conforme;
    }

    public void setConforme(boolean conforme) {
        this.conforme = conforme;
    }

    public BigDecimal getToleranciaAplicada() {
        return toleranciaAplicada;
    }

    public void setToleranciaAplicada(BigDecimal toleranciaAplicada) {
        this.toleranciaAplicada = toleranciaAplicada;
    }

    public BigDecimal getPorcentajeAvanceOrden() {
        return porcentajeAvanceOrden;
    }

    public void setPorcentajeAvanceOrden(BigDecimal porcentajeAvanceOrden) {
        this.porcentajeAvanceOrden = porcentajeAvanceOrden;
    }

    public boolean isCierreHabilitado() {
        return cierreHabilitado;
    }

    public void setCierreHabilitado(boolean cierreHabilitado) {
        this.cierreHabilitado = cierreHabilitado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
