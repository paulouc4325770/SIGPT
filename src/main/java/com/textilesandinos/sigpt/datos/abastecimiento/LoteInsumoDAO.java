package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;

import com.textilesandinos.sigpt.entidades.planta.LoteInsumo;

/**
 * Acceso a la trazabilidad de insumos por lote (regla RD6).
 */
public interface LoteInsumoDAO {

    /** Registra la trazabilidad entre un lote y el movimiento de kardex. */
    int insertar(LoteInsumo trazabilidad, String codUsuario);

    /** Trazabilidad de un lote. */
    List<LoteInsumo> listarPorLote(int idLote);
}
