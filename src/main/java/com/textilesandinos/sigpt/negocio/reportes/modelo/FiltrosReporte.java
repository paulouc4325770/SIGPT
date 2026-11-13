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

    /** Indica si la fecha cae dentro del rango pedido (sin rango, todo entra). */
    public boolean enRango(LocalDate fecha) {
        if (fechaDesde == null && fechaHasta == null) {
            return true;
        }
        if (fecha == null) {
            return false;
        }
        if (fechaDesde != null && fecha.isBefore(fechaDesde)) {
            return false;
        }
        return fechaHasta == null || !fecha.isAfter(fechaHasta);
    }

    /** Indica si hay algun criterio aplicado. */
    public boolean hayFiltros() {
        return idCliente != null || idOperario != null || idMaquina != null
                || fechaDesde != null || fechaHasta != null;
    }

    /**
     * Los filtros como cadena de consulta ({@code &idCliente=2&desde=...}).
     *
     * <p>Se usa para que los enlaces de exportacion conserven los criterios con
     * los que se genero el reporte en pantalla.
     */
    public String comoConsulta() {
        StringBuilder consulta = new StringBuilder();
        if (idCliente != null) {
            consulta.append("&idCliente=").append(idCliente);
        }
        if (idOperario != null) {
            consulta.append("&idOperario=").append(idOperario);
        }
        if (idMaquina != null) {
            consulta.append("&idMaquina=").append(idMaquina);
        }
        if (fechaDesde != null) {
            consulta.append("&desde=").append(fechaDesde);
        }
        if (fechaHasta != null) {
            consulta.append("&hasta=").append(fechaHasta);
        }
        return consulta.toString();
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
