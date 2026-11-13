package com.textilesandinos.sigpt.negocio.reportes;

import java.util.List;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Fabrica de reportes: resuelve el reporte por su codigo.
 */
@Component
public class FabricaReportes {

    private final List<ReporteBase> reportes;

    public FabricaReportes(List<ReporteBase> reportes) {
        this.reportes = reportes;
    }

    /** Reporte por su codigo (AVANCE_PEDIDOS, PRODUCTIVIDAD, ...). */
    public ReporteBase porCodigo(String codigo) {
        for (ReporteBase reporte : reportes) {
            if (reporte.codigo().equalsIgnoreCase(codigo)) {
                return reporte;
            }
        }
        throw new ReglaNegocioException("No existe el reporte con codigo " + codigo + ".");
    }

    /** Todos los reportes disponibles. */
    public List<ReporteBase> todos() {
        return reportes;
    }
}
