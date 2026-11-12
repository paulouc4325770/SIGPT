package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;

/**
 * Modelo de lectura de la orden de produccion en la pantalla de asignacion.
 *
 * <p>Une la orden con su pedido para que el Supervisor sepa que pedido esta
 * en planta.
 */
public class OrdenFila {

    private Integer idOrden;
    private String codOrden;
    private Integer idPedido;
    private String codPedido;
    private String nombreCliente;
    private BigDecimal metrosTotales;
    private BigDecimal metrosConsolidados;
    private String estado;

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public String getCodOrden() {
        return codOrden;
    }

    public void setCodOrden(String codOrden) {
        this.codOrden = codOrden;
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public BigDecimal getMetrosTotales() {
        return metrosTotales;
    }

    public void setMetrosTotales(BigDecimal metrosTotales) {
        this.metrosTotales = metrosTotales;
    }

    public BigDecimal getMetrosConsolidados() {
        return metrosConsolidados;
    }

    public void setMetrosConsolidados(BigDecimal metrosConsolidados) {
        this.metrosConsolidados = metrosConsolidados;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
