package com.textilesandinos.sigpt.entidades.abastecimiento;

/**
 * Proveedor homologado de materias primas: fibetra, ganadera o quimica.
 *
 * <p>Clase del dominio que refleja la tabla {@code proveedor}.
 */
public class Proveedor {

    private Integer idProveedor;

    /** Codigo de proveedor homologado */
    private String codigo;

    private String razonSocial;

    private String ruc;

    /** Agricola, Ganadera (cooperativas alpaqueras) o Quimica */
    private String tipo;

    private String contacto;

    private String correo;

    private String telefono;

    private String estado = "ACTIVO";

    public Proveedor() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
