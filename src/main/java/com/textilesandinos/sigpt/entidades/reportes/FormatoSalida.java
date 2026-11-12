package com.textilesandinos.sigpt.entidades.reportes;

/**
 * Formato de salida de un reporte: Pantalla, PDF o Excel (patron Estrategia).
 *
 * <p>Clase del dominio que refleja la tabla {@code formato_salida}.
 */
public class FormatoSalida {

    private Integer idFormato;

    /** PANTALLA, PDF, EXCEL */
    private String codigo;

    private String nombre;

    /** Clase de la estrategia de formato */
    private String claseImplementacion;

    private boolean estado = true;

    public FormatoSalida() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdFormato() {
        return idFormato;
    }

    public void setIdFormato(Integer idFormato) {
        this.idFormato = idFormato;
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

    public String getClaseImplementacion() {
        return claseImplementacion;
    }

    public void setClaseImplementacion(String claseImplementacion) {
        this.claseImplementacion = claseImplementacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
