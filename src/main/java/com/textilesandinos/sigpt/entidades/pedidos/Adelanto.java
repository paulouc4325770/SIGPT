package com.textilesandinos.sigpt.entidades.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Adelanto registrado sobre una cotizacion. Debe estar verificado para pasar el pedido a En Produccion.
 *
 * <p>Clase del dominio que refleja la tabla {@code adelanto}.
 */
public class Adelanto {

    private Integer idAdelanto;

    private Integer idCotizacion;

    private BigDecimal monto;

    private String moneda = "USD";

    /** Fecha en que se registro el adelanto */
    private LocalDate fechaAdelanto;

    /** Verificado por el Jefe de Produccion */
    private boolean verificado = false;

    /** Letra de credito o transferencia */
    private String referencia;

    public Adelanto() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdAdelanto() {
        return idAdelanto;
    }

    public void setIdAdelanto(Integer idAdelanto) {
        this.idAdelanto = idAdelanto;
    }

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

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
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

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
