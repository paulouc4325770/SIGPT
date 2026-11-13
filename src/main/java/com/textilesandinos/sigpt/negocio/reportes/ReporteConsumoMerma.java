package com.textilesandinos.sigpt.negocio.reportes;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.reportes.ReporteDAO;
import com.textilesandinos.sigpt.negocio.reportes.calculo.CalculoReporte;

/**
 * Reporte "Consumo de Insumos y Merma" (EF-1001-8).
 *
 * <p>La identidad del reporte; el calculo lo aporta la estrategia
 * {@code CalculoReporte}.
 */
@Component
public class ReporteConsumoMerma extends ReporteBase {

    public ReporteConsumoMerma(ReporteDAO reporteDAO, AuditoriaHelper auditoria,
            @Qualifier("calculoConsumoMerma") CalculoReporte calculo) {
        super(reporteDAO, auditoria, calculo);
    }

    @Override
    public String codigo() {
        return "CONSUMO_MERMA";
    }

    @Override
    public String titulo() {
        return "Consumo de Insumos y Merma";
    }
}
