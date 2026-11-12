package com.textilesandinos.sigpt.negocio.pedidos;

import java.math.BigDecimal;

import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;

/**
 * Resultado de registrar un adelanto: cuanto se ha cubierto del valor pactado
 * y si con ello ya se habilita la produccion (Regla R2).
 */
public class ResultadoAdelanto {

    private Adelanto adelanto;
    private BigDecimal porcentajeCubierto = BigDecimal.ZERO;
    private boolean produccionHabilitada;
    private OrdenProduccion ordenProduccion;
    private String mensaje;

    public Adelanto getAdelanto() {
        return adelanto;
    }

    public void setAdelanto(Adelanto adelanto) {
        this.adelanto = adelanto;
    }

    public BigDecimal getPorcentajeCubierto() {
        return porcentajeCubierto;
    }

    public void setPorcentajeCubierto(BigDecimal porcentajeCubierto) {
        this.porcentajeCubierto = porcentajeCubierto;
    }

    public boolean isProduccionHabilitada() {
        return produccionHabilitada;
    }

    public void setProduccionHabilitada(boolean produccionHabilitada) {
        this.produccionHabilitada = produccionHabilitada;
    }

    public OrdenProduccion getOrdenProduccion() {
        return ordenProduccion;
    }

    public void setOrdenProduccion(OrdenProduccion ordenProduccion) {
        this.ordenProduccion = ordenProduccion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
