package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Estado {@code REGISTRADO}: el pedido existe con su ficha tecnica, pero aun
 * no tiene cotizacion.
 *
 * <p>Puede pasar a Cotizado (cuando el Jefe de Produccion emite la cotizacion)
 * o anularse.
 */
public class EstadoRegistrado implements EstadoPedido {

    @Override
    public String codigo() {
        return "REGISTRADO";
    }

    @Override
    public String nombre() {
        return "Registrado";
    }

    @Override
    public boolean puedeTransitarA(EstadoPedido destino) {
        return destino instanceof EstadoCotizado || destino instanceof EstadoAnulado;
    }

    @Override
    public boolean esTerminal() {
        return false;
    }
}
