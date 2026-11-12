package com.textilesandinos.sigpt.negocio.pedidos.estados;

/**
 * Estado {@code COTIZADO}: la oferta fue emitida y esta vigente.
 *
 * <p>Pasa a En Produccion cuando el adelanto verificado alcanza el 40 % del
 * valor pactado (RD2). Tambien puede anularse.
 */
public class EstadoCotizado implements EstadoPedido {

    @Override
    public String codigo() {
        return "COTIZADO";
    }

    @Override
    public String nombre() {
        return "Cotizado";
    }

    @Override
    public boolean puedeTransitarA(EstadoPedido destino) {
        return destino instanceof EstadoEnProduccion || destino instanceof EstadoAnulado;
    }

    @Override
    public boolean esTerminal() {
        return false;
    }
}
