package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;

/**
 * Lineas de una Orden de Compra: insumo, cantidad y precio unitario.
 *
 * <p>Clase del dominio que refleja la tabla {@code orden_compra_detalle}.
 */
public class OrdenCompraDetalle {

    private Integer idOrdenCompra;

    private Integer idInsumo;

    private BigDecimal cantidad;

    private BigDecimal precioUnitario;

    public OrdenCompraDetalle() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(Integer idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
