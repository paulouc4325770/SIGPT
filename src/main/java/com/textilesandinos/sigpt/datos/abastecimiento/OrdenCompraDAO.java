package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompraDetalle;

/**
 * Acceso a las ordenes de compra y su detalle.
 */
public interface OrdenCompraDAO {

    /** Inserta la orden con su detalle en una sola transaccion. */
    int insertarConDetalle(OrdenCompra orden, List<OrdenCompraDetalle> detalle, String codUsuario);

    /** Lista las ordenes de compra, de la mas reciente a la mas antigua. */
    List<OrdenCompra> listarTodas();

    /** Busca una orden por su identificador. */
    Optional<OrdenCompra> buscarPorId(int idOrdenCompra);

    /** Detalle de una orden de compra. */
    List<OrdenCompraDetalle> listarDetalle(int idOrdenCompra);

    /** Cambia el estado de la orden (EMITIDA, CONFIRMADA, RECIBIDA, ANULADA). */
    int actualizarEstado(int idOrdenCompra, String estado);

    /** Registra la ruta del PDF generado de la orden. */
    int actualizarRutaPdf(int idOrdenCompra, String rutaPdf);

    /** Genera el siguiente codigo de orden de compra (OC-AAAA-NNN). */
    String siguienteCodigo();
}
