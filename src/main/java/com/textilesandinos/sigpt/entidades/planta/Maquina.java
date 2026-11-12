package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;

/**
 * Telar, urdidora o tintoreria. Su velocidad teorica por hora es la base de R-Varianza.
 *
 * <p>Clase del dominio que refleja la tabla {@code maquina}.
 */
public class Maquina {

    private Integer idMaquina;

    private String codigo;

    private String nombre;

    /** Telar circular, Telar rectilineo, Urdidora, Tintoreria */
    private String tipo;

    /** libre, ocupada, en mantenimiento, back-up */
    private String estado = "LIBRE";

    private BigDecimal velocidadTeoricaMtsHora = new BigDecimal("0.00");

    private BigDecimal horasPorTurno = new BigDecimal("8.00");

    private String observaciones;

    public Maquina() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getVelocidadTeoricaMtsHora() {
        return velocidadTeoricaMtsHora;
    }

    public void setVelocidadTeoricaMtsHora(BigDecimal velocidadTeoricaMtsHora) {
        this.velocidadTeoricaMtsHora = velocidadTeoricaMtsHora;
    }

    public BigDecimal getHorasPorTurno() {
        return horasPorTurno;
    }

    public void setHorasPorTurno(BigDecimal horasPorTurno) {
        this.horasPorTurno = horasPorTurno;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
