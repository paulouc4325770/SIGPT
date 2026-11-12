package com.textilesandinos.sigpt.entidades.seguridad;

/**
 * Perfil de acceso. Los perfiles son <b>datos</b>, no subclases de Usuario: los
 * ocho perfiles se diferencian por las opciones de menu que tienen asignadas.
 */
public class Perfil {

    private Integer idPerfil;
    private String codPerfil;
    private String nombrePerfil;
    private String descripcion;
    private boolean estado = true;

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getCodPerfil() {
        return codPerfil;
    }

    public void setCodPerfil(String codPerfil) {
        this.codPerfil = codPerfil;
    }

    public String getNombrePerfil() {
        return nombrePerfil;
    }

    public void setNombrePerfil(String nombrePerfil) {
        this.nombrePerfil = nombrePerfil;
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
