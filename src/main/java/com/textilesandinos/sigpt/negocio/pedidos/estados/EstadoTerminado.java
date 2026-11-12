package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Estado {@code TERMINADO}: el pedido alcanzo el 100 % del metraje consolidado
 * por control de calidad.
 *
 * <p>Es un estado final dentro del alcance del proyecto: el despacho se
 * excluyo (decision D-P4 / CHG-05).
 */
public class EstadoTerminado implements EstadoPedido {

    @Override
    public String codigo() {
        return "TERMINADO";
    }

    @Override
    public String nombre() {
        return "Terminado";
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
