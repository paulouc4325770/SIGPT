package com.textilesandinos.sigpt.entidades.seguridad;

/**
 * Opcion del menu. El menu es un arbol: {@code idOpcionRef} apunta a la opcion
 * padre y las hojas llevan la ruta {@code Area/Controlador/Accion}.
 */
public class Opcion {

    private Integer idOpcion;
    private String nombreOpcion;
    private String urlOpcion;
    private String rutaImagen;
    private int nroOrden;
    private Integer idOpcionRef;
    private boolean estado = true;

    /** Indica si la opcion es un grupo de menu (no tiene ruta propia). */
    public boolean esGrupo() {
        return urlOpcion == null || urlOpcion.isBlank();
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

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public int getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(int nroOrden) {
        this.nroOrden = nroOrden;
    }

    public Integer getIdOpcionRef() {
        return idOpcionRef;
    }

    public void setIdOpcionRef(Integer idOpcionRef) {
        this.idOpcionRef = idOpcionRef;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
