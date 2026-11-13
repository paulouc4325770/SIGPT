package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.util.List;

import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Strategy: el calculo de cada reporte.
 *
 * <p>Cada reporte gerencial tiene su propia estrategia de calculo. El
 * {@code ReporteBase} (Template Method) no sabe nada del detalle: solo llama a
 * la estrategia.
 */
public interface CalculoReporte {

    /** Codigo del reporte al que corresponde esta estrategia. */
    String codigo();

    /** Columnas de la tabla de detalle. */
    List<String> columnas();

    /** Filas del detalle, ya filtradas. */
    List<List<Object>> filas(FiltrosReporte filtros);

    /** Indicadores del tablero de control. */
    List<Indicador> indicadores(FiltrosReporte filtros);
}
