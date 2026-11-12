package com.textilesandinos.sigpt.negocio.planta;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de asignacion de operario y maquinaria.
 */
public class AsignacionFormulario {

    private Integer idOperario;
    private Integer idMaquina;
    private Integer idTurno;
    private LocalDate fecha;
    private Integer idOrdenProduccion;
    private BigDecimal metaMetros;

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

}
