package com.textilesandinos.sigpt.entidades.seguridad;

/**
 * Fila de la matriz de accesos: un perfil y una opcion de menu, con indication
 * de si el perfil la tiene asignada.
 *
 * <p>Es la lectura de {@code perfil_opcion} para la pantalla de administracion.
 */
public class MatrizAcceso {

    private Integer idPerfil;
    private String codPerfil;
    private String nombrePerfil;
    private Integer idOpcion;
    private String nombreOpcion;
    private String urlOpcion;
    private boolean asignado;

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

    public Integer getIdOpcion() {
        return idOpcion;
    }

    public void setIdOpcion(Integer idOpcion) {
        this.idOpcion = idOpcion;
    }

    public String getNombreOpcion() {
        return nombreOpcion;
    }

    public void setNombreOpcion(String nombreOpcion) {
        this.nombreOpcion = nombreOpcion;
    }

    public String getUrlOpcion() {
        return urlOpcion;
    }

    public void setUrlOpcion(String urlOpcion) {
        this.urlOpcion = urlOpcion;
    }

    public boolean isAsignado() {
        return asignado;
    }

    public void setAsignado(boolean asignado) {
        this.asignado = asignado;
    }
}
