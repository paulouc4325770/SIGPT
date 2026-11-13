package com.textilesandinos.sigpt.negocio.reportes.formato;

import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;
import com.textilesandinos.sigpt.utiles.exportacion.ExportadorTabla;

/**
 * Salida en hoja de calculo (formato HTML que Excel abre directamente).
 */
public class FormatoExcel implements EstrategiaFormato {

    @Override
    public String codigo() {
        return "EXCEL";
    }

    @Override
    public String nombre() {
        return "Hoja de calculo Excel";
    }

    @Override
    public byte[] escribir(ResultadoReporte reporte) {
        return ExportadorTabla.excelHtml(reporte.getColumnas(), reporte.getFilas());
    }

    @Override
    public String tipoContenido() {
        return "application/vnd.ms-excel";
    }

    @Override
    public String extension() {
        return "xls";
    }
}
