package com.textilesandinos.sigpt.utiles.notificacion;

import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;

/**
 * Envio de la Orden de Compra al proveedor (patron Adapter).
 *
 * <p>El dominio no conoce el transporte: depende de esta interfaz. Hoy se
 * implementa con un notificador que deja la salida registrada en el servidor
 * ({@link NotificadorArchivo}); si el negocio requiere SMTP, basta con
 * entregar otra implementacion sin tocar la capa de negocio.
 */
public interface NotificadorOrdenCompra {

    /**
     * Envia la orden de compra al proveedor.
     *
     * @param orden       orden emitida
     * @param proveedor   proveedor destinatario
     * @param pdf         contenido del PDF generado
     * @param rutaArchivo ruta donde quedo guardado el documento
     */
    ResultadoEnvio enviar(OrdenCompra orden, Proveedor proveedor, byte[] pdf, String rutaArchivo);

    /** Resultado del envio. */
    record ResultadoEnvio(boolean exitoso, String detalle) {
    }
}
