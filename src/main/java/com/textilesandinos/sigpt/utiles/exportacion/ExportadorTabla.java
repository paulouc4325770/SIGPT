package com.textilesandinos.sigpt.utiles.exportacion;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Exporta un conjunto de datos a CSV o a una hoja de calculo.
 *
 * <p>El enunciado pide que el resultado de una consulta se pueda exportar a
 * Excel o a PDF. En este entregable se.exporta a CSV y a Excel en formato HTML
 * (decision CHG-07): el PDF se resuelve en el entregable 3, donde ya se necesita
 * para la orden de compra, y asi el proyecto no suma dependencias binarias.
 *
 * <p>El CSV usa punto y coma como separador, que es el que espera Excel en
 * español, y antepone la marca BOM para que reconozca el UTF-8.
 */
public final class ExportadorTabla {

    private static final String SEPARADOR = ";";
    private static final String BOM = "\uFEFF";

    private ExportadorTabla() {
    }

    /** Nombre del archivo que se descargara. */
    public static String nombreArchivo(String prefijo, String extension) {
        String fecha = LocalDate.now().toString();
        return prefijo + "-" + fecha + "." + extension;
    }

    /**
     * Genera el contenido CSV.
     *
     * @param columnas cabeceras de la tabla
     * @param filas    filas de la tabla; cada elemento es una celda
     */
    public static String csv(List<String> columnas, List<List<Object>> filas) {
        StringBuilder salida = new StringBuilder(BOM);
        salida.append(cabeceraCsv(columnas)).append("\r\n");
        for (List<Object> fila : filas) {
            StringBuilder linea = new StringBuilder();
            for (int i = 0; i < columnas.size(); i++) {
                if (i > 0) {
                    linea.append(SEPARADOR);
                }
                linea.append(celdaCsv(i < fila.size() ? fila.get(i) : null));
            }
            salida.append(linea).append("\r\n");
        }
        return salida.toString();
    }

    /**
     * Genera una hoja de calculo en formato HTML: Excel la abre directamente y
     * respeta los tipos porcolumna.
     */
    public static byte[] excelHtml(List<String> columnas, List<List<Object>> filas) {
        StringBuilder html = new StringBuilder();
        html.append("<html xmlns:x=\"urn:schemas-microsoft-com:office:excel\">");
        html.append("<head><meta charset=\"UTF-8\"></head><body>");
        html.append("<table border=\"1\"><thead><tr>");
        for (String columna : columnas) {
            html.append("<th style=\"background:#DDE6F0\">").append(escapar(columna)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (List<Object> fila : filas) {
            html.append("<tr>");
            for (int i = 0; i < columnas.size(); i++) {
                Object valor = i < fila.size() ? fila.get(i) : null;
                html.append("<td>").append(escapar(texto(valor))).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String cabeceraCsv(List<String> columnas) {
        StringBuilder cabecera = new StringBuilder();
        for (int i = 0; i < columnas.size(); i++) {
            if (i > 0) {
                cabecera.append(SEPARADOR);
            }
            cabecera.append(celdaCsv(columnas.get(i)));
        }
        return cabecera.toString();
    }

    private static String celdaCsv(Object valor) {
        String texto = texto(valor);
        boolean necesitaComillas = texto.contains(SEPARADOR) || texto.contains("\"")
                || texto.contains("\n") || texto.contains("\r");
        if (!necesitaComillas) {
            return texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    /** Convierte un valor a su texto de pantalla. */
    public static String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        if (valor instanceof LocalDate fecha) {
            return fecha.toString();
        }
        if (valor instanceof LocalDateTime momento) {
            return momento.toString();
        }
        return valor.toString();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
