package com.textilesandinos.sigpt.entidades.abastecimiento;

import java.math.BigDecimal;

/**
 * Consumo teorico planificado por Orden de Produccion. Es la base del 15% de RD4.
 *
 * <p>Clase del dominio que refleja la tabla {@code orden_produccion_insumo}.
 */
public class OrdenProduccionInsumo {

    private Integer idOrdenProduccion;

    private Integer idInsumo;

    /** Consumo teorico planificado (base de RD4) */
    private BigDecimal cantidadTeorica;

    public OrdenProduccionInsumo() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdOrdenProduccion() {
        return idOrdenProduccion;
    }

    public void setIdOrdenProduccion(Integer idOrdenProduccion) {
        this.idOrdenProduccion = idOrdenProduccion;
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public BigDecimal getCantidadTeorica() {
        return cantidadTeorica;
    }

    public void setCantidadTeorica(BigDecimal cantidadTeorica) {
        this.cantidadTeorica = cantidadTeorica;
    }
}
