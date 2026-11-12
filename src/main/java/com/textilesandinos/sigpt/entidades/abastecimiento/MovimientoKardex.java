package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Movimiento de inventario (ingreso, salida o ajuste). Es la base del Kardex valorizado.
 *
 * <p>Clase del dominio que refleja la tabla {@code movimiento_kardex}.
 */
public class MovimientoKardex {

    private Integer idMovimiento;

    private Integer idInsumo;

    /** INGRESO, SALIDA, AJUSTE */
    private String tipoMovimiento;

    private BigDecimal cantidad;

    private BigDecimal precioUnitario = new BigDecimal("0.00");

    private Integer idOrdenCompra;

    private Integer idOrdenProduccion;

    private LocalDate fecha;

    private String observaciones;

    public MovimientoKardex() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
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

    public Integer getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(Integer idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
