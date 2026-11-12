package com.textilesandinos.sigpt.negocio.pedidos;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del formulario de registro de adelanto sobre una cotizacion.
 */
public class AdelantoFormulario {

    private Integer idCotizacion;
    private BigDecimal monto;
    private String referencia;
    private LocalDate fechaAdelanto;
    private boolean verificado;

    public Integer getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Integer idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDate getFechaAdelanto() {
        return fechaAdelanto;
    }

    public void setFechaAdelanto(LocalDate fechaAdelanto) {
        this.fechaAdelanto = fechaAdelanto;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

}
