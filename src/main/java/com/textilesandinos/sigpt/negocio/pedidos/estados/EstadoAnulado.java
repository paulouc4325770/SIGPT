package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Estado {@code ANULADO}: el pedido se cancelo antes de entrar a planta.
 *
 * <p>Es un estado final.
 */
public class EstadoAnulado implements EstadoPedido {

    @Override
    public String codigo() {
        return "ANULADO";
    }

    @Override
    public String nombre() {
        return "Anulado";
    }

    @Override
    public boolean puedeTransitarA(EstadoPedido destino) {
        return false;
    }

    @Override
    public boolean esTerminal() {
        return true;
    }
}
