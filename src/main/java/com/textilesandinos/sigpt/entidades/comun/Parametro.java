package com.textilesandinos.sigpt.entidades.comun;

/**
 * Parametro de negocio. Los umbrales del sistema (1000 m, 40 %, 16 h, 15 %,
 * 3 intentos, 15 minutos) viven aqui y no en el codigo.
 */
public class Parametro {

    private Integer idParametro;
    private String codigo;
    private String nombre;
    private String valor;
    private String tipoDato = "TEXTO";
    private String descripcion;
    private boolean estado = true;

    public Integer getIdParametro() {
        return idParametro;
    }

    public void setIdParametro(Integer idParametro) {
        this.idParametro = idParametro;
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

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
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
