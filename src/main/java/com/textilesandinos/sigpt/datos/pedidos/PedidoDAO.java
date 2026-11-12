package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;

/**
 * Acceso a los pedidos y a su ficha tecnica.
 */
public interface PedidoDAO {

    /** Lista pedidos con los criterios de busqueda de la bandeja. */
    List<PedidoFila> listar(String codigo, String estado, Integer idCliente, LocalDate desde,
            LocalDate hasta);

    /** Cuenta los pedidos por estado para el tablero de control. */
    ResumenPedidos resumir();

    /** Busca un pedido por su identificador. */
    Optional<Pedido> buscarPorId(int idPedido);

    /** Busca la ficha tecnica de un pedido. */
    Optional<FichaTecnica> buscarFichaTecnica(int idPedido);

    /**
     * Inserta el pedido y su ficha tecnica en una sola transaccion.
     *
     * @return identificador del pedido generado
     */
    int insertarConFicha(Pedido pedido, FichaTecnica ficha, String codUsuario);

    /** Cambia el estado del pedido. */
    int actualizarEstado(int idPedido, String estado, String codUsuario);

    /** Actualiza los metros consolidados por control de calidad. */
    int actualizarMetrosConsolidados(int idPedido, BigDecimal metros);

    /** Genera el siguiente codigo de pedido del ano en curso (PED-AAAA-NNNN). */
    String siguienteCodigo();
}
