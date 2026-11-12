package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Orden de Compra formal al proveedor. Se genera en PDF y se envia por correo (patron Adapter).
 *
 * <p>Clase del dominio que refleja la tabla {@code orden_compra}.
 */
public class OrdenCompra {

    private Integer idOrdenCompra;

    private String codigo;

    private Integer idProveedor;

    private Integer idSolicitud;

    private Integer idCotizacionProveedor;

    private BigDecimal montoTotal;

    private String moneda = "USD";

    private LocalDate fechaEmision;

    private LocalDate fechaEntregaEsperada;

    /** PDF generado y enviado al proveedor (Adapter de correo) */
    private String rutaPdf;

    private String estado = "EMITIDA";

    private String observaciones;

    public OrdenCompra() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(Integer idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
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

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdCotizacionProveedor() {
        return idCotizacionProveedor;
    }

    public void setIdCotizacionProveedor(Integer idCotizacionProveedor) {
        this.idCotizacionProveedor = idCotizacionProveedor;
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

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaEntregaEsperada() {
        return fechaEntregaEsperada;
    }

    public void setFechaEntregaEsperada(LocalDate fechaEntregaEsperada) {
        this.fechaEntregaEsperada = fechaEntregaEsperada;
    }

    public String getRutaPdf() {
        return rutaPdf;
    }

    public void setRutaPdf(String rutaPdf) {
        this.rutaPdf = rutaPdf;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
