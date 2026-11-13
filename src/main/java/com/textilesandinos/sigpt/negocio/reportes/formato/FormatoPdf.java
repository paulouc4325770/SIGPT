package com.textilesandinos.sigpt.negocio.reportes.formato;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;
import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Salida en PDF (OpenPDF).
 */
public class FormatoPdf implements EstrategiaFormato {

    private static final Color AZUL = new Color(31, 56, 100);

    @Override
    public String codigo() {
        return "PDF";
    }

    @Override
    public String nombre() {
        return "Documento PDF";
    }

    @Override
    public byte[] escribir(ResultadoReporte reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 42, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            PdfPCell celdaTitulo = new PdfPCell(new Phrase(reporte.getTitulo(),
                    new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE)));
            celdaTitulo.setBackgroundColor(AZUL);
            celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaTitulo.setPadding(8);
            PdfPTable titulo = new PdfPTable(1);
            titulo.setWidthPercentage(100);
            titulo.addCell(celdaTitulo);
            documento.add(titulo);

            if (!reporte.getIndicadores().isEmpty()) {
                documento.add(new Phrase(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
                documento.add(indicadores(reporte.getIndicadores()));
            }

            documento.add(new Phrase(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            documento.add(tabla(reporte.getColumnas(), reporte.getFilas()));
            documento.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte "
                    + reporte.getCodigo(), e);
        }
        return salida.toByteArray();
    }

    private PdfPTable indicadores(List<Indicador> indicadores) {
        PdfPTable tabla = new PdfPTable(indicadores.size());
        tabla.setWidthPercentage(100);
        for (Indicador indicador : indicadores) {
            PdfPCell celda = new PdfPCell();
            celda.addElement(new Phrase(indicador.getTitulo(),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            celda.addElement(new Phrase(" "));
            celda.addElement(new Phrase(indicador.getValor(),
                    new Font(Font.HELVETICA, 12, Font.BOLD)));
            if (indicador.getDetalle() != null) {
                celda.addElement(new Phrase(" "));
                celda.addElement(new Phrase(indicador.getDetalle(),
                        new Font(Font.HELVETICA, 7, Font.NORMAL)));
            }
            celda.setBackgroundColor(new Color(238, 243, 249));
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(6);
            tabla.addCell(celda);
        }
        return tabla;
    }

    private PdfPTable tabla(List<String> columnas, List<List<Object>> filas) {
        PdfPTable tabla = new PdfPTable(columnas.size());
        tabla.setWidthPercentage(100);
        for (String columna : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(columna,
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            celda.setBackgroundColor(new Color(224, 230, 240));
            celda.setPadding(4);
            tabla.addCell(celda);
        }
        for (List<Object> fila : filas) {
            for (Object valor : fila) {
                tabla.addCell(new PdfPCell(new Phrase(valor == null ? "" : valor.toString(),
                        new Font(Font.HELVETICA, 8, Font.NORMAL))));
            }
        }
        return tabla;
    }

    @Override
    public String tipoContenido() {
        return "application/pdf";
    }

    @Override
    public String extension() {
        return "pdf";
    }
}
