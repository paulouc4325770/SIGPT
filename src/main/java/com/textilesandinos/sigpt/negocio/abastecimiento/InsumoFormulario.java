package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.math.BigDecimal;

/**
 * Datos del insumo (EF-1001-5).
 */
public class InsumoFormulario {

    private Integer idProveedor;
    private String nombre;
    private String tipo;
    private String unidadMedida;
    private BigDecimal stockMinimo;
    private BigDecimal precioUnitario;
    private String rutaMsds;
    private String estado;

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getRutaMsds() {
        return rutaMsds;
    }

    public void setRutaMsds(String rutaMsds) {
        this.rutaMsds = rutaMsds;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}