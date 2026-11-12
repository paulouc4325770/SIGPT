package com.textilesandinos.sigpt.entidades.comun;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tipo de cambio con vigencia por fecha. Permite valorizar pedidos de
 * exportacion en moneda extranjera (CHG-02).
 */
public class TipoCambio {

    private Integer idTipoCambio;
    private String monedaOrigen = "PEN";
    private String monedaDestino = "USD";
    private BigDecimal valor;
    private LocalDate fechaVigencia;
    private boolean estado = true;

    public Integer getIdTipoCambio() {
        return idTipoCambio;
    }

    public void setIdTipoCambio(Integer idTipoCambio) {
        this.idTipoCambio = idTipoCambio;
    }

    public String getMonedaOrigen() {
        return monedaOrigen;
    }

    public void setMonedaOrigen(String monedaOrigen) {
        this.monedaOrigen = monedaOrigen;
    }

    public String getMonedaDestino() {
        return monedaDestino;
    }

    public void setMonedaDestino(String monedaDestino) {
        this.monedaDestino = monedaDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public void setFechaVigencia(LocalDate fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
