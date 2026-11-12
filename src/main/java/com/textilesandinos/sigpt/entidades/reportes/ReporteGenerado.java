package com.textilesandinos.sigpt.entidades.reportes;

import java.time.LocalDateTime;

/**
 * Auditoria de la generacion de un reporte: usuario, filtros, formato y fecha.
 *
 * <p>Clase del dominio que refleja la tabla {@code reporte_generado}.
 */
public class ReporteGenerado {

    private Long idReporteGenerado;

    private Integer idReporte;

    private String codUsuario;

    /** Filtros aplicados, en JSON */
    private String filtros;

    private String formatoSalida;

    private LocalDateTime fechaGeneracion;

    public ReporteGenerado() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Long getIdReporteGenerado() {
        return idReporteGenerado;
    }

    public void setIdReporteGenerado(Long idReporteGenerado) {
        this.idReporteGenerado = idReporteGenerado;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        this.codUsuario = codUsuario;
    }

    public String getFiltros() {
        return filtros;
    }

    public void setFiltros(String filtros) {
        this.filtros = filtros;
    }

    public String getFormatoSalida() {
        return formatoSalida;
    }

    public void setFormatoSalida(String formatoSalida) {
        this.formatoSalida = formatoSalida;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
}
