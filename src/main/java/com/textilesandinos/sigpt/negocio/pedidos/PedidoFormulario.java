package com.textilesandinos.sigpt.negocio.pedidos;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de registro de pedido (ficha tecnica).
 */
public class PedidoFormulario {

    private Integer idCliente;
    private String tipoHilado;
    private BigDecimal gramajeM2;
    private String colorPantone;
    private String composicion;
    private BigDecimal metros;
    private BigDecimal toleranciaColor;
    private LocalDate fechaEntrega;
    private String observaciones;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
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

    public String getComposicion() {
        return composicion;
    }

    public void setComposicion(String composicion) {
        this.composicion = composicion;
    }

    public BigDecimal getMetros() {
        return metros;
    }

    public void setMetros(BigDecimal metros) {
        this.metros = metros;
    }

    public BigDecimal getToleranciaColor() {
        return toleranciaColor;
    }

    public void setToleranciaColor(BigDecimal toleranciaColor) {
        this.toleranciaColor = toleranciaColor;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

}
