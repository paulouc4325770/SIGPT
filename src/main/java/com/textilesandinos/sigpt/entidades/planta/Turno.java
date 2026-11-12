package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Turno de trabajo: manana, tarde o amanecida.
 *
 * <p>Clase del dominio que refleja la tabla {@code turno}.
 */
public class Turno {

    private Integer idTurno;

    /** MANANA, TARDE, AMANECIDA */
    private String codigo;

    private String nombre;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private BigDecimal horas;

    public Turno() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }
}
