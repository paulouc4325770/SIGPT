package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;

/**
 * Acceso a las ordenes de produccion internas.
 */
public interface OrdenProduccionDAO {

    /** Inserta la orden de produccion y devuelve su identificador. */
    int insertar(OrdenProduccion orden, String codUsuario);

    /** Busca una orden por su identificador. */
    Optional<OrdenProduccion> buscarPorId(int idOrden);

    /** Busca la orden de produccion emitida para un pedido. */
    Optional<OrdenProduccion> buscarPorPedido(int idPedido);

    /** Lista las ordenes en produccion, para la pantalla de asignacion. */
    List<OrdenFila> listarEnProduccion();

    /** Actualiza los metros consolidados de la orden. */
    int actualizarMetrosConsolidados(int idOrden, BigDecimal metros);

    /** Cambia el estado de la orden (EN_PRODUCCION, TERMINADO, ANULADA). */
    int actualizarEstado(int idOrden, String estado);

    /** Genera el siguiente codigo de orden de produccion (OP-AAAA-NNNN). */
    String siguienteCodigo();
}
