package com.textilesandinos.sigpt.negocio.reportes;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.reportes.ReporteDAO;
import com.textilesandinos.sigpt.negocio.reportes.calculo.CalculoReporte;

/**
 * Reporte "Eficiencia Industrial" (EF-1001-8).
 *
 * <p>La identidad del reporte; el calculo lo aporta la estrategia
 * {@code CalculoReporte}.
 */
@Component
public class ReporteEficiencia extends ReporteBase {

    public ReporteEficiencia(ReporteDAO reporteDAO, AuditoriaHelper auditoria,
            @Qualifier("calculoEficiencia") CalculoReporte calculo) {
        super(reporteDAO, auditoria, calculo);
    }

    @Override
    public String codigo() {
        return "EFICIENCIA_IND";
    }

    @Override
    public String titulo() {
        return "Eficiencia Industrial";
    }
}
