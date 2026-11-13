package com.textilesandinos.sigpt.negocio.reportes.modelo;

/**
 * Indicador del tablero de control de un reporte.
 */
public class Indicador {

    private String titulo;
    private String valor;
    private String detalle;

    public Indicador() {
    }

    public Indicador(String titulo, String valor, String detalle) {
        this.titulo = titulo;
        this.valor = valor;
        this.detalle = detalle;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
