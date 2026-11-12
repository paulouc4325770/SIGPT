package com.textilesandinos.sigpt.entidades.planta;

/**
 * Incidencia reportada en planta: averia, parada o falla de telar.
 *
 * <p>Clase del dominio que refleja la tabla {@code incidencia}.
 */
public class Incidencia {

    private Integer idIncidencia;

    private Integer idAvance;

    private Integer idMaquina;

    /** AVERIA, PARADA, FALLA_TELAR, OTRO */
    private String tipo;

    private String descripcion;

    /** ABIERTA, EN_ATENCION, CERRADA */
    private String estado = "ABIERTA";

    public Incidencia() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Integer idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
