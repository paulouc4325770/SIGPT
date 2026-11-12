package com.textilesandinos.sigpt.negocio.pedidos.estados;

import java.util.List;

/**
 * Fabrica de los estados del pedido (parte del patron State).
 *
 * <p>Concentra el unico punto donde se reconoce cada estado, de modo que el
 * resto del sistema trabaja siempre contra la interfaz {@link EstadoPedido}.
 */
public final class FabricaEstadoPedido {

    private static final List<EstadoPedido> TODOS = List.of(
            new EstadoRegistrado(),
            new EstadoCotizado(),
            new EstadoEnProduccion(),
            new EstadoTerminado(),
            new EstadoAnulado());

    private FabricaEstadoPedido() {
    }

    /** Estado correspondiente a un codigo de la base de datos. */
    public static EstadoPedido de(String codigo) {
        if (codigo != null) {
            for (EstadoPedido estado : TODOS) {
                if (estado.codigo().equalsIgnoreCase(codigo)) {
                    return estado;
                }
            }
        }
        throw new IllegalArgumentException("Estado de pedido desconocido: " + codigo);
    }

    /** Todos los estados del ciclo de vida, en orden. */
    public static List<EstadoPedido> todos() {
        return TODOS;
    }

    /** Indica si el pedido puede pasar del estado actual al destino. */
    public static boolean puedeTransitar(String codigoActual, String codigoDestino) {
        return de(codigoActual).puedeTransitarA(de(codigoDestino));
    }
}
