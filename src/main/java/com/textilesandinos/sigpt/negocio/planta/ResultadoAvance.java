package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;

/**
 * Resultado de registrar un avance: la varianza frente a la velocidad teorica
 * del telar y si corresponde emitir la alerta de baja productividad.
 */
public class ResultadoAvance {

    private Integer idAvance;
    private Integer idLote;
    private BigDecimal metrosTeoricos = BigDecimal.ZERO;
    private BigDecimal varianzaPct = BigDecimal.ZERO;
    private boolean alertaVarianza;
    private String mensajeAlerta;
    private String mensaje;

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public BigDecimal getMetrosTeoricos() {
        return metrosTeoricos;
    }

    public void setMetrosTeoricos(BigDecimal metrosTeoricos) {
        this.metrosTeoricos = metrosTeoricos;
    }

    public BigDecimal getVarianzaPct() {
        return varianzaPct;
    }

    public void setVarianzaPct(BigDecimal varianzaPct) {
        this.varianzaPct = varianzaPct;
    }

    public boolean isAlertaVarianza() {
        return alertaVarianza;
    }

    public void setAlertaVarianza(boolean alertaVarianza) {
        this.alertaVarianza = alertaVarianza;
    }

    public String getMensajeAlerta() {
        return mensajeAlerta;
    }

    public void setMensajeAlerta(String mensajeAlerta) {
        this.mensajeAlerta = mensajeAlerta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
