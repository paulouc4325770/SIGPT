package com.textilesandinos.sigpt.entidades.reportes;

/**
 * Catalogo de los cuatro reportes gerenciales iniciales del sistema.
 *
 * <p>Clase del dominio que refleja la tabla {@code reporte_gerencial}.
 */
public class ReporteGerencial {

    private Integer idReporte;

    private String codigo;

    private String nombre;

    private String descripcion;

    private boolean estado = true;

    public ReporteGerencial() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
