package com.textilesandinos.sigpt.negocio.pedidos;

import java.math.BigDecimal;

import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;

/**
 * Resultado de emitir una cotizacion: la cotizacion creada, su equivalencia
 * en soles al tipo de cambio vigente y el estado del pedido.
 */
public class ResultadoCotizacion {

    private Cotizacion cotizacion;
    private BigDecimal montoEnPen = BigDecimal.ZERO;
    private BigDecimal tipoCambioAplicado;
    private boolean produccionHabilitada;
    private String mensaje;

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }

    public BigDecimal getMontoEnPen() {
        return montoEnPen;
    }

    public void setMontoEnPen(BigDecimal montoEnPen) {
        this.montoEnPen = montoEnPen;
    }

    public BigDecimal getTipoCambioAplicado() {
        return tipoCambioAplicado;
    }

    public void setTipoCambioAplicado(BigDecimal tipoCambioAplicado) {
        this.tipoCambioAplicado = tipoCambioAplicado;
    }

    public boolean isProduccionHabilitada() {
        return produccionHabilitada;
    }

    public void setProduccionHabilitada(boolean produccionHabilitada) {
        this.produccionHabilitada = produccionHabilitada;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
