package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parte diario y cierre de lote, generado cuando el avance llega al 100%.
 *
 * <p>Clase del dominio que refleja la tabla {@code parte_diario_produccion}.
 */
public class ParteDiarioProduccion {

    private Integer idParte;

    private Integer idOrdenProduccion;

    private LocalDate fecha;

    private BigDecimal horasHombre = new BigDecimal("0.00");

    private BigDecimal horasMaquina = new BigDecimal("0.00");

    private BigDecimal metros = new BigDecimal("0.00");

    private String observaciones;

    public ParteDiarioProduccion() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
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

    public BigDecimal getHorasHombre() {
        return horasHombre;
    }

    public void setHorasHombre(BigDecimal horasHombre) {
        this.horasHombre = horasHombre;
    }

    public BigDecimal getHorasMaquina() {
        return horasMaquina;
    }

    public void setHorasMaquina(BigDecimal horasMaquina) {
        this.horasMaquina = horasMaquina;
    }

    public BigDecimal getMetros() {
        return metros;
    }

    public void setMetros(BigDecimal metros) {
        this.metros = metros;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
