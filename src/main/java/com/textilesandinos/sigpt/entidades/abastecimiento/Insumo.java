package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;

/**
 * Materia prima o insumo del catalogo. Los quimicos y tintes exigen su Hoja de Seguridad (RD5).
 *
 * <p>Clase del dominio que refleja la tabla {@code insumo}.
 */
public class Insumo {

    private Integer idInsumo;

    private String codigo;

    private Integer idProveedor;

    private String nombre;

    /** FIBRA, TINTE, QUIMICO, CAJA */
    private String tipo;

    private String unidadMedida = "KG";

    /** Umbral de alerta de stock (R-Stock) */
    private BigDecimal stockMinimo = new BigDecimal("0.00");

    /** Mantenido en la misma transaccion del kardex */
    private BigDecimal stockActual = new BigDecimal("0.00");

    private BigDecimal precioUnitario = new BigDecimal("0.00");

    /** Ruta del archivo MSDS en el servidor. Obligatorio para QUIMICO y TINTE (RD5) */
    private String rutaMsds;

    private String estado = "ACTIVO";

    public Insumo() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

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

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
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
