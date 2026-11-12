package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cotizacion emitida al cliente. Su vigencia y el adelanto recibido habilitan el inicio de la produccion (RD2).
 *
 * <p>Clase del dominio que refleja la tabla {@code cotizacion}.
 */
public class Cotizacion {

    private Integer idCotizacion;

    private Integer idPedido;

    /** Valor pactado: base del 40% de RD2 */
    private BigDecimal montoTotal;

    private String moneda = "USD";

    private BigDecimal costoInsumos = new BigDecimal("0.00");

    /** Tiempo de ciclo estimado */
    private BigDecimal horasCiclo = new BigDecimal("0.00");

    private LocalDate fechaEmision;

    /** Vigencia de la oferta (15 dias por parametro) */
    private LocalDate fechaVigencia;

    private String estado = "EMITIDA";

    public Cotizacion() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Integer idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

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

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public void setFechaVigencia(LocalDate fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
