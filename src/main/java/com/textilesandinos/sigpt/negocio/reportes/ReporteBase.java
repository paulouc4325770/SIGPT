package com.textilesandinos.sigpt.negocio.reportes;

import java.time.LocalDateTime;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.reportes.ReporteDAO;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.negocio.reportes.calculo.CalculoReporte;
import com.textilesandinos.sigpt.negocio.reportes.formato.EstrategiaFormato;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Template Method: el esqueleto del algoritmo de generacion de un reporte.
 *
 * <p>El metodo {@link #generar} fija el orden de los pasos: calcular
 * indicadores, calcular filas, escribir el resultado en el formato elegido
 * (Bridge) y registrar el reporte en el historial. Las subclases solo aportan
 * su identidad y su estrategia de calculo (Strategy).
 */
public abstract class ReporteBase {

    protected final ReporteDAO reporteDAO;
    protected final AuditoriaHelper auditoria;
    private final CalculoReporte calculo;

    protected ReporteBase(ReporteDAO reporteDAO, AuditoriaHelper auditoria,
            CalculoReporte calculo) {
        this.reporteDAO = reporteDAO;
        this.auditoria = auditoria;
        this.calculo = calculo;
    }

    /** Codigo del reporte, tal como figura en la tabla {@code reporte_gerencial}. */
    public abstract String codigo();

    /** Titulo del reporte. */
    public abstract String titulo();

    /** Estrategia de calculo asociada. */
    public CalculoReporte calculo() {
        return calculo;
    }

    /**
     * Genera el reporte. Es el metodo plantilla: no se reescribe en las
     * subclases.
     */
    public final ResultadoReporte generar(FiltrosReporte filtros, EstrategiaFormato formato,
            String codUsuario) {
        ResultadoReporte resultado = new ResultadoReporte();
        resultado.setCodigo(codigo());
        resultado.setTitulo(titulo());
        resultado.setColumnas(calculo.columnas());
        resultado.setIndicadores(calculo.indicadores(filtros));
        resultado.setFilas(calculo.filas(filtros));
        resultado.setFormato(formato.codigo());
        resultado.setContenido(formato.escribir(resultado));
        resultado.setNombreArchivo(codigo().toLowerCase() + "." + formato.extension());
        registrarEnHistorial(filtros, formato, codUsuario);
        return resultado;
    }

    /** Registra la generacion del reporte en {@code reporte_generado}. */
    private void registrarEnHistorial(FiltrosReporte filtros, EstrategiaFormato formato,
            String codUsuario) {
        ReporteGenerado generado = new ReporteGenerado();
        generado.setCodUsuario(codUsuario == null ? "sistema" : codUsuario);
        generado.setFiltros(filtros.resumir());
        generado.setFormatoSalida(formato.codigo());
        generado.setFechaGeneracion(LocalDateTime.now());
        reporteDAO.listarReportes().stream()
                .filter(reporte -> reporte.getCodigo().equals(codigo()))
                .findFirst()
                .ifPresent(reporte -> generado.setIdReporte(reporte.getIdReporte()));
        if (generado.getIdReporte() != null) {
            reporteDAO.registrarGenerado(generado);
        }
        auditoria.registrar(codUsuario, "REPORTES", "GENERAR_REPORTE", "reporte_gerencial",
                codigo(), "Formato " + formato.codigo() + " | " + filtros.resumir());
    }
}
