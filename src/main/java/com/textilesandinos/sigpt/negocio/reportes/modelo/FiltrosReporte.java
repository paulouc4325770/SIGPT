package com.textilesandinos.sigpt.negocio.reportes.modelo;

import java.time.LocalDate;

/**
 * Filtros de una consulta de reportes.
 */
public class FiltrosReporte {

    private Integer idCliente;
    private Integer idOperario;
    private Integer idMaquina;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String codUsuario;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(Integer idOperario) {
        this.idOperario = idOperario;
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        this.codUsuario = codUsuario;
    }

    /** Texto de los filtros, para guardarlos en el historial. */
    public String resumir() {
        StringBuilder texto = new StringBuilder();
        texto.append("cliente=").append(idCliente == null ? "todos" : idCliente);
        texto.append(";operario=").append(idOperario == null ? "todos" : idOperario);
        texto.append(";maquina=").append(idMaquina == null ? "todas" : idMaquina);
        texto.append(";desde=").append(fechaDesde == null ? "inicio" : fechaDesde);
        texto.append(";hasta=").append(fechaHasta == null ? "hoy" : fechaHasta);
        return texto.toString();
    }
}
