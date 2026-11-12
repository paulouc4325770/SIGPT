package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Orden de produccion interna. Solo existe cuando el pedido pasa a En Produccion con el 40% de adelanto.
 *
 * <p>Clase del dominio que refleja la tabla {@code orden_produccion}.
 */
public class OrdenProduccion {

    private Integer idOrden;

    private String codOrden;

    private Integer idPedido;

    private BigDecimal metrosTotales;

    private BigDecimal metrosConsolidados = new BigDecimal("0.00");

    /** Base del reporte de Eficiencia Industrial */
    private BigDecimal horasMaquinaPrevistas = new BigDecimal("0.00");

    private BigDecimal horasHombrePrevistas = new BigDecimal("0.00");

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private String estado = "EN_PRODUCCION";

    public OrdenProduccion() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public String getCodOrden() {
        return codOrden;
    }

    public void setCodOrden(String codOrden) {
        this.codOrden = codOrden;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public BigDecimal getMetrosTotales() {
        return metrosTotales;
    }

    public void setMetrosTotales(BigDecimal metrosTotales) {
        this.metrosTotales = metrosTotales;
    }

    public BigDecimal getMetrosConsolidados() {
        return metrosConsolidados;
    }

    public void setMetrosConsolidados(BigDecimal metrosConsolidados) {
        this.metrosConsolidados = metrosConsolidados;
    }

    public BigDecimal getHorasMaquinaPrevistas() {
        return horasMaquinaPrevistas;
    }

    public void setHorasMaquinaPrevistas(BigDecimal horasMaquinaPrevistas) {
        this.horasMaquinaPrevistas = horasMaquinaPrevistas;
    }

    public BigDecimal getHorasHombrePrevistas() {
        return horasHombrePrevistas;
    }

    public void setHorasHombrePrevistas(BigDecimal horasHombrePrevistas) {
        this.horasHombrePrevistas = horasHombrePrevistas;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
