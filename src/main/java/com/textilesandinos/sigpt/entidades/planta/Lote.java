package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;

/**
 * Lote de tela producido. Debe mantener la trazabilidad de su materia prima (RD6).
 *
 * <p>Clase del dominio que refleja la tabla {@code lote}.
 */
public class Lote {

    private Integer idLote;

    private String codigo;

    private Integer idOrdenProduccion;

    private Integer idAsignacion;

    private BigDecimal metros = new BigDecimal("0.00");

    /** PENDIENTE_INSPECCION, CONFORME, MERMA */
    private String estado = "PENDIENTE_INSPECCION";

    public Lote() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
    }

    public Integer getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Integer idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public BigDecimal getMetros() {
        return metros;
    }

    public void setMetros(BigDecimal metros) {
        this.metros = metros;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
