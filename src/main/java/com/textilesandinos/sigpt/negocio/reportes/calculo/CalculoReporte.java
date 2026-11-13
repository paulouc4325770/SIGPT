package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.util.List;

import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltroReporte;
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

    /**
     * Filtros que esta estrategia aplica realmente sobre sus datos.
     *
     * <p>La pantalla solo muestra esos controles: asi el usuario no elige un
     * criterio que el reporte va a ignorar.
     */
    default List<FiltroReporte> filtrosSoportados() {
        return List.of(FiltroReporte.CLIENTE, FiltroReporte.OPERARIO,
                FiltroReporte.MAQUINA, FiltroReporte.FECHAS);
    }

    /** Filas del detalle, ya filtradas. */
    List<List<Object>> filas(FiltrosReporte filtros);

    /** Indicadores del tablero de control. */
    List<Indicador> indicadores(FiltrosReporte filtros);
}
