package com.textilesandinos.sigpt.entidades.planta;

/**
 * Recurso de planta que opera telares o cubas de teñido. Se asocia a un Usuario; no hereda de el.
 *
 * <p>Clase del dominio que refleja la tabla {@code operario}.
 */
public class Operario {

    private Integer idOperario;

    private String codigo;

    private String nombres;

    /** Telar, TeÃ±idor */
    private String especialidad;

    /** Asociacion, NO herencia: el usuario es la cuenta de acceso */
    private Integer idUsuario;

    private String estado = "ACTIVO";

    public Operario() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(Integer idOperario) {
        this.idOperario = idOperario;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
