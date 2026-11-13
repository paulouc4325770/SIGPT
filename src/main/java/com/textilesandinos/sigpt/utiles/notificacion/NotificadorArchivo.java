package com.textilesandinos.sigpt.utiles.notificacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;

/**
 * Notificador que deja el envio registrado en el servidor.
 *
 * <p>Es la implementacion por defecto del adaptador: guarda el PDF y un
 * mensaje con los datos del envio en la carpeta configurada, de modo que el
 * Jefe de Logistica pueda revisar que salio la orden. Sustituirlo por SMTP es
 * question de entregar otra clase que implemente {@link NotificadorOrdenCompra}.
 */
@Component
public class NotificadorArchivo implements NotificadorOrdenCompra {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorArchivo.class);
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String carpetaEnvios;

    public NotificadorArchivo(
            @Value("${sigpt.envios.carpeta:./data/envios}") String carpetaEnvios) {
        this.carpetaEnvios = carpetaEnvios;
    }

    @Override
    public ResultadoEnvio enviar(OrdenCompra orden, Proveedor proveedor, byte[] pdf,
            String rutaArchivo) {
        try {
            Path carpeta = Path.of(carpetaEnvios);
            Files.createDirectories(carpeta);

            String sello = LocalDateTime.now().format(SELLO);
            Path destinoPdf = carpeta.resolve(orden.getCodigo() + "-" + sello + ".pdf");
            Files.write(destinoPdf, pdf);

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Para: ").append(proveedor.getCorreo() == null
                    ? proveedor.getRazonSocial() : proveedor.getCorreo()).append('\n');
            mensaje.append("Orden de compra: ").append(orden.getCodigo()).append('\n');
            mensaje.append("Monto: ").append(orden.getMontoTotal().toPlainString()).append(' ')
                    .append(orden.getMoneda()).append('\n');
            mensaje.append("Fecha: ").append(LocalDateTime.now()).append('\n');
            mensaje.append("Documento adjunto: ").append(destinoPdf.getFileName()).append('\n');

            Path destinoMensaje = carpeta.resolve(orden.getCodigo() + "-" + sello + ".eml");
            Files.write(destinoMensaje, mensaje.toString().getBytes(StandardCharsets.UTF_8));

            LOG.info("Orden de compra {} enviada a {} (documento en {})",
                    orden.getCodigo(), proveedor.getRazonSocial(), destinoPdf);
            return new ResultadoEnvio(true, "Orden enviada al proveedor. Documento: "
                    + destinoPdf.toString());
        } catch (IOException e) {
            LOG.error("No se pudo enviar la orden de compra {}", orden.getCodigo(), e);
            return new ResultadoEnvio(false, "No se pudo registrar el envio: " + e.getMessage());
        }
    }
}
