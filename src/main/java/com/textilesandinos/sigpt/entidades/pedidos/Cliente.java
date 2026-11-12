package com.textilesandinos.sigpt.entidades.pedidos;

/**
 * Cliente que solicita ordenes de produccion (marcas corporativas, disenadores y retail).
 *
 * <p>Clase del dominio que refleja la tabla {@code cliente}.
 */
public class Cliente {

    private Integer idCliente;

    private String codigo;

    private String razonSocial;

    /** Unico: el sistema no permite clientes duplicados */
    private String rucTaxId;

    private String pais;

    private String contacto;

    private String correo;

    private String telefono;

    private String estado = "ACTIVO";

    public Cliente() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
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

    public String getRucTaxId() {
        return rucTaxId;
    }

    public void setRucTaxId(String rucTaxId) {
        this.rucTaxId = rucTaxId;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
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
