package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Patron State: cada estado del pedido sabe a que estados puede pasar.
 *
 * <p>El ciclo de vida del pedido (EF-1001-3) es:
 * {@code Registrado -> Cotizado -> En Produccion -> Terminado}, con la
 * possibility de anular el pedido mientras aun no ha entrado a planta.
 *
 * <p>Las transiciones validas viven en las clases que implementan esta
 * interfaz, no en un {@code switch} del presentador ni en la base de datos: el
 * control de cambios de estados es parte del dominio.
 */
public interface EstadoPedido {

    /** Codigo del estado tal como se guarda en la base de datos. */
    String codigo();

    /** Nombre del estado para la pantalla. */
    String nombre();

    /** Indica si el pedido puede pasar a este estado. */
    boolean puedeTransitarA(EstadoPedido destino);

    /** Indica que el estado ya no admite salidas. */
    boolean esTerminal();
}
