package com.textilesandinos.sigpt.negocio.reportes.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de generar un reporte: sus indicadores, sus filas y el contenido
 * ya escrito en el formato de salida elegido (Bridge).
 */
public class ResultadoReporte {

    private String codigo;
    private String titulo;
    private List<String> columnas = new ArrayList<>();
    private List<List<Object>> filas = new ArrayList<>();
    private List<Indicador> indicadores = new ArrayList<>();
    private String formato;
    private byte[] contenido = new byte[0];
    private String nombreArchivo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public List<List<Object>> getFilas() {
        return filas;
    }

    public void setFilas(List<List<Object>> filas) {
        this.filas = filas;
    }

    public List<Indicador> getIndicadores() {
        return indicadores;
    }

    public void setIndicadores(List<Indicador> indicadores) {
        this.indicadores = indicadores;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
}
