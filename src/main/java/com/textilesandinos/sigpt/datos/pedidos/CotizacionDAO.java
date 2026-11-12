package com.textilesandinos.sigpt.datos.pedidos;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;

/**
 * Acceso a las cotizaciones de los pedidos.
 */
public interface CotizacionDAO {

    /** Lista las cotizaciones de un pedido, de la mas reciente a la mas antigua. */
    List<Cotizacion> listarPorPedido(int idPedido);

    /** Lista todas las cotizaciones emitidas. */
    List<Cotizacion> listarTodas();

    /** Busca una cotizacion por su identificador. */
    Optional<Cotizacion> buscarPorId(int idCotizacion);

    /** Inserta una cotizacion y devuelve su identificador. */
    int insertar(Cotizacion cotizacion, String codUsuario);

    /** Cambia el estado de la cotizacion (EMITIDA, ACEPTADA, VENCIDA, RECHAZADA, ANULADA). */
    int actualizarEstado(int idCotizacion, String estado);

    /** Marca como vencidas las cotizaciones cuya vigencia ya paso. */
    int marcarVencidas(LocalDate hoy);
}
