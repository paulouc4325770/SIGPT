package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Ficha completa de un pedido: datos del pedido, ficha tecnica, cotizaciones,
 * adelantos y orden de produccion emitida.
 *
 * <p>Se arma en la capa de negocio con varias consultas y es lo que muestra la
 * pantalla de consulta del pedido.
 */
public class PedidoDetalle {

    private Pedido pedido;
    private Cliente cliente;
    private FichaTecnica fichaTecnica;
    private List<Cotizacion> cotizaciones = new ArrayList<>();
    private List<Adelanto> adelantos = new ArrayList<>();
    private OrdenProduccion ordenProduccion;
    private BigDecimal adelantoTotal = new BigDecimal("0.00");
    private BigDecimal adelantoVerificado = new BigDecimal("0.00");
    private BigDecimal porcentajeAdelanto = new BigDecimal("0.00");
    private BigDecimal montoPactado = new BigDecimal("0.00");

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public FichaTecnica getFichaTecnica() {
        return fichaTecnica;
    }

    public void setFichaTecnica(FichaTecnica fichaTecnica) {
        this.fichaTecnica = fichaTecnica;
    }

    public List<Cotizacion> getCotizaciones() {
        return cotizaciones;
    }

    public void setCotizaciones(List<Cotizacion> cotizaciones) {
        this.cotizaciones = cotizaciones;
    }

    public List<Adelanto> getAdelantos() {
        return adelantos;
    }

    public void setAdelantos(List<Adelanto> adelantos) {
        this.adelantos = adelantos;
    }

    public OrdenProduccion getOrdenProduccion() {
        return ordenProduccion;
    }

    public void setOrdenProduccion(OrdenProduccion ordenProduccion) {
        this.ordenProduccion = ordenProduccion;
    }

    public BigDecimal getAdelantoTotal() {
        return adelantoTotal;
    }

    public void setAdelantoTotal(BigDecimal adelantoTotal) {
        this.adelantoTotal = adelantoTotal;
    }

    public BigDecimal getAdelantoVerificado() {
        return adelantoVerificado;
    }

    public void setAdelantoVerificado(BigDecimal adelantoVerificado) {
        this.adelantoVerificado = adelantoVerificado;
    }

    public BigDecimal getPorcentajeAdelanto() {
        return porcentajeAdelanto;
    }

    public void setPorcentajeAdelanto(BigDecimal porcentajeAdelanto) {
        this.porcentajeAdelanto = porcentajeAdelanto;
    }

    public BigDecimal getMontoPactado() {
        return montoPactado;
    }

    public void setMontoPactado(BigDecimal montoPactado) {
        this.montoPactado = montoPactado;
    }
}
