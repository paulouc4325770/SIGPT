package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;

import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenProduccionInsumo;

/**
 * Acceso al consumo teorico planificado de insumos por orden de produccion.
 *
 * <p>Es la base de la regla RD4: la varianza del consumo real frente a esta
 * cifra teorica.
 */
public interface OrdenProduccionInsumoDAO {

    /** Lista el consumo teorico de una orden de produccion. */
    List<OrdenProduccionInsumo> listarPorOrden(int idOrdenProduccion);

    /** Registra el consumo teorico de un insumo en una orden. */
    int insertar(OrdenProduccionInsumo consumo, String codUsuario);
}
