package com.textilesandinos.sigpt.negocio.reportes.formato;

import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Salida en pantalla: el contenido lo dibuja la plantilla Thymeleaf, asi que
 * aqui no se genera ningun archivo.
 */
public class FormatoPantalla implements EstrategiaFormato {

    @Override
    public String codigo() {
        return "PANTALLA";
    }

    @Override
    public String nombre() {
        return "Visualizacion en pantalla";
    }

    @Override
    public byte[] escribir(ResultadoReporte reporte) {
        return new byte[0];
    }

    @Override
    public String tipoContenido() {
        return "text/html;charset=UTF-8";
    }

    @Override
    public String extension() {
        return "html";
    }
}
