package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;

/**
 * Ficha tecnica del pedido: tipo de hilado, gramaje, color Pantone, composicion y tolerancia de color (RD7).
 *
 * <p>Clase del dominio que refleja la tabla {@code ficha_tecnica}.
 */
public class FichaTecnica {

    private Integer idFicha;

    /** Composicion: la ficha tecnica no existe sin su pedido */
    private Integer idPedido;

    private String tipoHilado;

    private BigDecimal gramajeM2;

    private String colorPantone;

    private String composicion;

    private BigDecimal metros;

    private BigDecimal toleranciaColor = new BigDecimal("2.00");

    private String observaciones;

    public FichaTecnica() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdFicha() {
        return idFicha;
    }

    public void setIdFicha(Integer idFicha) {
        this.idFicha = idFicha;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
