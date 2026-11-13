package com.textilesandinos.sigpt.negocio.reportes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.reportes.ReporteDAO;
import com.textilesandinos.sigpt.entidades.reportes.FormatoSalida;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGerencial;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.reportes.formato.FormatoExcel;
import com.textilesandinos.sigpt.negocio.reportes.formato.FormatoPantalla;
import com.textilesandinos.sigpt.negocio.reportes.formato.FormatoPdf;
import com.textilesandinos.sigpt.negocio.reportes.formato.EstrategiaFormato;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Fachada de los reportes gerenciales (EF-1001-8).
 *
 * <p>Resuelve el formato de salida (Bridge) a partir de la tabla
 * {@code formato_salida} y el reporte a partir de su codigo.
 */
@Service
public class ReporteLN {

    private final ReporteDAO reporteDAO;
    private final FabricaReportes fabrica;

    public ReporteLN(ReporteDAO reporteDAO, FabricaReportes fabrica) {
        this.reporteDAO = reporteDAO;
        this.fabrica = fabrica;
    }

    /** Reportes disponibles en el catalogo. */
    public List<ReporteGerencial> listarReportes() {
        return reporteDAO.listarReportes();
    }

    /** Formatos de salida disponibles. */
    public List<FormatoSalida> listarFormatos() {
        return reporteDAO.listarFormatos();
    }

    /** Historial de reportes generados. */
    public List<ReporteGenerado> historico(int limite) {
        return reporteDAO.listarGenerados(limite);
    }

    /**
     * Genera un reporte en el formato indicado.
     *
     * @param codigo    codigo del reporte
     * @param formato   codigo del formato (PANTALLA, PDF, EXCEL)
     * @param filtros   filtros aplicados
     * @param codUsuario usuario que lo genera
     */
    public ResultadoReporte generar(String codigo, String formato, FiltrosReporte filtros,
            String codUsuario) {
        ReporteBase reporte = fabrica.porCodigo(codigo);
        EstrategiaFormato salida = resolverFormato(formato);
        return reporte.generar(filtros, salida, codUsuario);
    }

    /** Resuelve el formato de salida; el Bridge permite cambiarlo sin tocar el calculo. */
    private EstrategiaFormato resolverFormato(String codigo) {
        String solicitado = codigo == null || codigo.isBlank() ? "PANTALLA" : codigo;
        for (FormatoSalida formato : reporteDAO.listarFormatos()) {
            if (formato.getCodigo().equalsIgnoreCase(solicitado)) {
                return crear(formato.getCodigo());
            }
        }
        throw new ReglaNegocioException("El formato de salida " + solicitado + " no esta "
                + "habilitado en el sistema.");
    }

    private EstrategiaFormato crear(String codigo) {
        List<EstrategiaFormato> formatos = new ArrayList<>();
        formatos.add(new FormatoPantalla());
        formatos.add(new FormatoPdf());
        formatos.add(new FormatoExcel());
        for (EstrategiaFormato formato : formatos) {
            if (formato.codigo().equalsIgnoreCase(codigo)) {
                return formato;
            }
        }
        throw new ReglaNegocioException("El formato de salida " + codigo + " no esta implementado.");
    }
}
