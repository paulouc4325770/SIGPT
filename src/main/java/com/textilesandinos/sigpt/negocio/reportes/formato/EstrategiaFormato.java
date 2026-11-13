package com.textilesandinos.sigpt.negocio.reportes.formato;

import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Bridge: formato de salida de un reporte.
 *
 * <p>Es una abstraccion independiente de los reportes: el mismo resultado se
 * puede ver en pantalla, exportar a PDF o a Excel sin tocar el calculo.
 */
public interface EstrategiaFormato {

    /** Codigo del formato, tal como figura en la tabla {@code formato_salida}. */
    String codigo();

    /** Nombre para la pantalla. */
    String nombre();

    /** Escribe el reporte en este formato. */
    byte[] escribir(ResultadoReporte reporte);

    /** Tipo de contenido HTTP del archivo generado. */
    String tipoContenido();

    /** Extension del archivo generado. */
    String extension();
}
