package com.textilesandinos.sigpt.negocio.pedidos;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de emision de cotizacion.
 */
public class CotizacionFormulario {

    private Integer idPedido;
    private BigDecimal montoTotal;
    private String moneda;
    private BigDecimal costoInsumos;
    private BigDecimal horasCiclo;
    private LocalDate fechaEmision;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getCostoInsumos() {
        return costoInsumos;
    }

    public void setCostoInsumos(BigDecimal costoInsumos) {
        this.costoInsumos = costoInsumos;
    }

    public BigDecimal getHorasCiclo() {
        return horasCiclo;
    }

    public void setHorasCiclo(BigDecimal horasCiclo) {
        this.horasCiclo = horasCiclo;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

}
