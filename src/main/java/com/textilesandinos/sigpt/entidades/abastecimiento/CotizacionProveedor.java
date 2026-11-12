package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;

/**
 * Respuesta de un proveedor a una solicitud de cotizacion (precio, plazo y certificaciones).
 *
 * <p>Clase del dominio que refleja la tabla {@code cotizacion_proveedor}.
 */
public class CotizacionProveedor {

    private Integer idCotizacionProveedor;

    private Integer idSolicitud;

    private Integer idProveedor;

    private BigDecimal precioUnitario;

    private String unidad = "KG";

    /** Criterio de la matriz costo-beneficio-tiempo */
    private Integer tiempoEntregaDias = 0;

    private String certificaciones;

    private String observaciones;

    private String estado = "RECIBIDA";

    public CotizacionProveedor() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdCotizacionProveedor() {
        return idCotizacionProveedor;
    }

    public void setIdCotizacionProveedor(Integer idCotizacionProveedor) {
        this.idCotizacionProveedor = idCotizacionProveedor;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Integer getTiempoEntregaDias() {
        return tiempoEntregaDias;
    }

    public void setTiempoEntregaDias(Integer tiempoEntregaDias) {
        this.tiempoEntregaDias = tiempoEntregaDias;
    }

    public String getCertificaciones() {
        return certificaciones;
    }

    public void setCertificaciones(String certificaciones) {
        this.certificaciones = certificaciones;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
