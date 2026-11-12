package com.textilesandinos.sigpt.entidades.planta;

import java.math.BigDecimal;

/**
 * Vinculo entre un lote y los movimientos de Kardex que consumio (trazabilidad RD6).
 *
 * <p>Clase del dominio que refleja la tabla {@code lote_insumo}.
 */
public class LoteInsumo {

    private Integer idLote;

    private Integer idMovimientoKardex;

    private BigDecimal cantidadConsumida;

    public LoteInsumo() {
        // Constructor vacio: lo usa el mapeo de datos y el binding de formularios.
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public Integer getIdMovimientoKardex() {
        return idMovimientoKardex;
    }

    public void setIdMovimientoKardex(Integer idMovimientoKardex) {
        this.idMovimientoKardex = idMovimientoKardex;
    }

    public BigDecimal getCantidadConsumida() {
        return cantidadConsumida;
    }

    public void setCantidadConsumida(BigDecimal cantidadConsumida) {
        this.cantidadConsumida = cantidadConsumida;
    }
}
