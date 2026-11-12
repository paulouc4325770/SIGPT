package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Estado {@code EN_PRODUCCION}: el pedido esta en planta y tiene una Orden de
 * Produccion emitida.
 *
 * <p>Solo puede pasar a Terminado, cuando el cierre de lote completa el 100 %
 * del metraje. Una vez en planta el pedido no se anula: lo que se anula es la
 * orden de produccion.
 */
public class EstadoEnProduccion implements EstadoPedido {

    @Override
    public String codigo() {
        return "EN_PRODUCCION";
    }

    @Override
    public String nombre() {
        return "En Produccion";
    }

    @Override
    public boolean puedeTransitarA(EstadoPedido destino) {
        return destino instanceof EstadoTerminado;
    }

    @Override
    public boolean esTerminal() {
        return false;
    }
}
