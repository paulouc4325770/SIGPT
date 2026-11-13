package com.textilesandinos.sigpt.datos.reportes;

import java.time.LocalDateTime;
import java.util.List;

import com.textilesandinos.sigpt.entidades.reportes.FormatoSalida;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGerencial;

/**
 * Acceso al catalogo de reportes gerenciales y al historial de reportes
 * generados.
 */
public interface ReporteDAO {

    /** Lista los reportes habilitados. */
    List<ReporteGerencial> listarReportes();

    /** Lista los formatos de salida habilitados. */
    List<FormatoSalida> listarFormatos();

    /** Registra un reporte generado con sus filtros y formato. */
    int registrarGenerado(ReporteGenerado reporte);

    /** Lista el historial de reportes generados. */
    List<ReporteGenerado> listarGenerados(int limite);
}
