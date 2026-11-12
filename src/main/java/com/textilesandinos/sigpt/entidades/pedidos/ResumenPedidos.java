package com.textilesandinos.sigpt.entidades.pedidos;

/**
 * Tablero de control de la bandeja de pedidos: cuantos hay en cada estado.
 *
 * <p>Es el semaforo de la pantalla de pedidos.
 */
public class ResumenPedidos {

    private int registrados;
    private int cotizados;
    private int enProduccion;
    private int terminados;
    private int anulados;
    private int total;

    public int getRegistrados() {
        return registrados;
    }

    public void setRegistrados(int registrados) {
        this.registrados = registrados;
    }

    public int getCotizados() {
        return cotizados;
    }

    public void setCotizados(int cotizados) {
        this.cotizados = cotizados;
    }

    public int getEnProduccion() {
        return enProduccion;
    }

    public void setEnProduccion(int enProduccion) {
        this.enProduccion = enProduccion;
    }

    public int getTerminados() {
        return terminados;
    }

    public void setTerminados(int terminados) {
        this.terminados = terminados;
    }

    public int getAnulados() {
        return anulados;
    }

    public void setAnulados(int anulados) {
        this.anulados = anulados;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
