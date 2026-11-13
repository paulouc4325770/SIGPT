package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;

import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;

/**
 * Acceso al kardex de movimientos de insumos.
 */
public interface MovimientoKardexDAO {

    /**
     * Registra el movimiento y actualiza el stock del insumo en la misma
     * transaccion (el stock nunca queda desincronizado del kardex).
     */
    int registrar(MovimientoKardex movimiento, String codUsuario);

    /** Lista los movimientos de un insumo, del mas reciente al mas antiguo. */
    List<MovimientoKardex> listarPorInsumo(int idInsumo);

    /** Lista todos los movimientos, del mas reciente al mas antiguo. */
    List<MovimientoKardex> listarTodos();

    /** Movimientos de salida asociados a una orden de produccion. */
    List<MovimientoKardex> listarPorOrden(int idOrdenProduccion);
}
