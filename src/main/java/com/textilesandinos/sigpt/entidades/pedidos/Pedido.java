package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pedido textil con su ficha tecnica y su ciclo de vida: Registrado, Cotizado, En Produccion, Terminado y Anulado.
 *
 * <p>Clase del dominio que refleja la tabla {@code pedido}.
 */
public class Pedido {

    private Integer idPedido;

    private String codPedido;

    private Integer idCliente;

    private BigDecimal metros;

    private LocalDate fechaEntrega;

    private String estado = "REGISTRADO";

    private BigDecimal metrosConsolidados = new BigDecimal("0.00");

    /** Fecha en que el pedido paso a En Produccion */
    private LocalDate fechaRegistroOperativa;

    public Pedido() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getCodPedido() {
        return codPedido;
    }

    public void setCodPedido(String codPedido) {
        this.codPedido = codPedido;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public BigDecimal getMetros() {
        return metros;
    }

    public void setMetros(BigDecimal metros) {
        this.metros = metros;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getMetrosConsolidados() {
        return metrosConsolidados;
    }

    public void setMetrosConsolidados(BigDecimal metrosConsolidados) {
        this.metrosConsolidados = metrosConsolidados;
    }

    public LocalDate getFechaRegistroOperativa() {
        return fechaRegistroOperativa;
    }

    public void setFechaRegistroOperativa(LocalDate fechaRegistroOperativa) {
        this.fechaRegistroOperativa = fechaRegistroOperativa;
    }
}
