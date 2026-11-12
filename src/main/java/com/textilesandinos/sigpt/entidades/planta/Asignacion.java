package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Asignacion de un operario a una maquina en un turno, con su meta de metros.
 *
 * <p>Clase del dominio que refleja la tabla {@code asignacion}.
 */
public class Asignacion {

    private Integer idAsignacion;

    private Integer idOperario;

    private Integer idMaquina;

    private Integer idTurno;

    private LocalDate fecha;

    private Integer idOrdenProduccion;

    private BigDecimal metaMetros = new BigDecimal("0.00");

    private BigDecimal horasAsignadas = new BigDecimal("8.00");

    private String estado = "ASIGNADA";

    public Asignacion() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Integer idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public Integer getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(Integer idOperario) {
        this.idOperario = idOperario;
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
    }

    public BigDecimal getMetaMetros() {
        return metaMetros;
    }

    public void setMetaMetros(BigDecimal metaMetros) {
        this.metaMetros = metaMetros;
    }

    public BigDecimal getHorasAsignadas() {
        return horasAsignadas;
    }

    public void setHorasAsignadas(BigDecimal horasAsignadas) {
        this.horasAsignadas = horasAsignadas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
