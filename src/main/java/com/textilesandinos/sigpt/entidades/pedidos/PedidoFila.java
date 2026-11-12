package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de lectura de la bandeja de pedidos: une el pedido con su cliente y
 * su ficha tecnica para que la tabla se pinte con una sola consulta.
 *
 * <p>No es una entidad de la base de datos: es una vista de pantalla.
 */
public class PedidoFila {

    private Integer idPedido;
    private String codPedido;
    private Integer idCliente;
    private String nombreCliente;
    private String tipoHilado;
    private BigDecimal gramajeM2;
    private String colorPantone;
    private BigDecimal metros;
    private BigDecimal metrosConsolidados;
    private LocalDate fechaEntrega;
    private String estado;

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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTipoHilado() {
        return tipoHilado;
    }

    public void setTipoHilado(String tipoHilado) {
        this.tipoHilado = tipoHilado;
    }

    public BigDecimal getGramajeM2() {
        return gramajeM2;
    }

    public void setGramajeM2(BigDecimal gramajeM2) {
        this.gramajeM2 = gramajeM2;
    }

    public String getColorPantone() {
        return colorPantone;
    }

    public void setColorPantone(String colorPantone) {
        this.colorPantone = colorPantone;
    }

    public BigDecimal getMetros() {
        return metros;
    }

    public void setMetros(BigDecimal metros) {
        this.metros = metros;
    }

    public BigDecimal getMetrosConsolidados() {
        return metrosConsolidados;
    }

    public void setMetrosConsolidados(BigDecimal metrosConsolidados) {
        this.metrosConsolidados = metrosConsolidados;
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
}
