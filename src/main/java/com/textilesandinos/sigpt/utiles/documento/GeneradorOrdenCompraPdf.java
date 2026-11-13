package com.textilesandinos.sigpt.utiles.documento;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompraDetalle;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;

/**
 * Genera el PDF de la Orden de Compra (EF-1001-5).
 *
 * <p>Es el documento que se envia al proveedor: por eso se genera con
 * OpenPDF, una biblioteca libre bajo licencia MPL/LGPL compatible con Java 17.
 */
@Component
public class GeneradorOrdenCompraPdf {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color AZUL = new Color(31, 56, 100);

    /**
     * Genera el PDF de la orden de compra.
     *
     * @param orden    orden de compra
     * @param proveedor proveedor destinatario
     * @param detalle  lineas de la orden
     * @param insumos  catalogo, para poner el nombre de cada insumo
     * @return los bytes del documento
     */
    public byte[] generar(OrdenCompra orden, Proveedor proveedor,
            List<OrdenCompraDetalle> detalle, List<Insumo> insumos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 42, 42, 48, 42);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(titulo("TEXTILES ANDINOS S.A.C. - Orden de Compra"));
            documento.add(new Paragraph(" ", new Font(Font.HELVETICA, 4, Font.NORMAL)));
            documento.add(datosGenerales(orden, proveedor));
            documento.add(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            documento.add(lineas(detalle, insumos));
            documento.add(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            documento.add(totales(orden, detalle));

            documento.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la orden de compra "
                    + orden.getCodigo(), e);
        }
        return salida.toByteArray();
    }

    private PdfPTable titulo(String texto) {
        Font fuente = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(AZUL);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(8);
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.addCell(celda);
        return tabla;
    }

    private Paragraph datosGenerales(OrdenCompra orden, Proveedor proveedor) {
        StringBuilder datos = new StringBuilder();
        datos.append("Codigo: ").append(orden.getCodigo()).append('\n');
        datos.append("Fecha de emision: ").append(orden.getFechaEmision()).append('\n');
        if (orden.getFechaEntregaEsperada() != null) {
            datos.append("Entrega esperada: ").append(orden.getFechaEntregaEsperada()).append('\n');
        }
        datos.append("Proveedor: ").append(proveedor.getRazonSocial())
                .append(" (").append(proveedor.getCodigo()).append(")\n");
        if (proveedor.getRuc() != null) {
            datos.append("RUC: ").append(proveedor.getRuc()).append('\n');
        }
        datos.append("Moneda: ").append(orden.getMoneda()).append('\n');
        if (orden.getObservaciones() != null) {
            datos.append("Observaciones: ").append(orden.getObservaciones());
        }
        Font fuente = new Font(Font.HELVETICA, 10, Font.NORMAL);
        return new Paragraph(datos.toString(), fuente);
    }

    private PdfPTable lineas(List<OrdenCompraDetalle> detalle, List<Insumo> insumos) {
        PdfPTable tabla = new PdfPTable(new float[] {1.2f, 3.4f, 1.4f, 1.6f, 1.8f});
        tabla.setWidthPercentage(100);
        String[] cabeceras = {"Codigo", "Insumo", "Cantidad", "Precio unit.", "Subtotal"};
        for (String cabecera : cabeceras) {
            tabla.addCell(celda(cabecera, true));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenCompraDetalle linea : detalle) {
            BigDecimal subtotal = linea.getCantidad().multiply(linea.getPrecioUnitario());
            total = total.add(subtotal);
            tabla.addCell(celda(codigoInsumo(linea.getIdInsumo(), insumos), false));
            tabla.addCell(celda(nombreInsumo(linea.getIdInsumo(), insumos), false));
            tabla.addCell(celda(linea.getCantidad().toPlainString(), false));
            tabla.addCell(celda(linea.getPrecioUnitario().toPlainString(), false));
            tabla.addCell(celda(subtotal.toPlainString(), false));
        }
        return tabla;
    }

    private Paragraph totales(OrdenCompra orden, List<OrdenCompraDetalle> detalle) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenCompraDetalle linea : detalle) {
            total = total.add(linea.getCantidad().multiply(linea.getPrecioUnitario()));
        }
        StringBuilder texto = new StringBuilder();
        texto.append("Total de la orden: ").append(total.toPlainString()).append(' ')
                .append(orden.getMoneda()).append('\n');
        texto.append("Documento generado por el SIGPT el ")
                .append(java.time.LocalDate.now().format(FECHA)).append('.');
        return new Paragraph(texto.toString(), new Font(Font.HELVETICA, 10, Font.BOLD));
    }

    private PdfPCell celda(String texto, boolean cabecera) {
        Font fuente = new Font(Font.HELVETICA, 9, cabecera ? Font.BOLD : Font.NORMAL);
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setPadding(5);
        if (cabecera) {
            celda.setBackgroundColor(new Color(224, 230, 240));
        }
        return celda;
    }

    private String codigoInsumo(int idInsumo, List<Insumo> insumos) {
        for (Insumo insumo : insumos) {
            if (insumo.getIdInsumo() == idInsumo) {
                return insumo.getCodigo();
            }
        }
        return String.valueOf(idInsumo);
    }

    private String nombreInsumo(int idInsumo, List<Insumo> insumos) {
        for (Insumo insumo : insumos) {
            if (insumo.getIdInsumo() == idInsumo) {
                return insumo.getNombre();
            }
        }
        return "";
    }
}
