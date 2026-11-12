package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de metros producidos y merma por turno. Su varianza se contrasta con la velocidad teorica del telar.
 *
 * <p>Clase del dominio que refleja la tabla {@code registro_avance}.
 */
public class RegistroAvance {

    private Integer idAvance;

    private Integer idAsignacion;

    private Integer idOrdenProduccion;

    private Integer idOperario;

    private Integer idMaquina;

    private Integer idTurno;

    private LocalDate fecha;

    private BigDecimal metrosProducidos = new BigDecimal("0.00");

    private BigDecimal metrosMerma = new BigDecimal("0.00");

    private BigDecimal varianzaPct = new BigDecimal("0.00");

    /** PENDIENTE, CONFORME, NO_CONFORME */
    private String estadoCalidad = "PENDIENTE";

    private String observacion;

    public RegistroAvance() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public Integer getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Integer idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
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

    public BigDecimal getMetrosProducidos() {
        return metrosProducidos;
    }

    public void setMetrosProducidos(BigDecimal metrosProducidos) {
        this.metrosProducidos = metrosProducidos;
    }

    public BigDecimal getMetrosMerma() {
        return metrosMerma;
    }

    public void setMetrosMerma(BigDecimal metrosMerma) {
        this.metrosMerma = metrosMerma;
    }

    public BigDecimal getVarianzaPct() {
        return varianzaPct;
    }

    public void setVarianzaPct(BigDecimal varianzaPct) {
        this.varianzaPct = varianzaPct;
    }

    public String getEstadoCalidad() {
        return estadoCalidad;
    }

    public void setEstadoCalidad(String estadoCalidad) {
        this.estadoCalidad = estadoCalidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
