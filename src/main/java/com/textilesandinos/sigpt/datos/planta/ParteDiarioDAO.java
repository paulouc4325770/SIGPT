package com.textilesandinos.sigpt.datos.planta;

import java.util.List;

import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;

/**
 * Acceso a los partes diarios de produccion.
 */
public interface ParteDiarioDAO {

    /** Guarda el parte diario; si ya existe para esa fecha, lo actualiza. */
    int guardar(ParteDiarioProduccion parte, String codUsuario);

    /** Partes diarios de una orden de produccion. */
    List<ParteDiarioProduccion> listarPorOrden(int idOrdenProduccion);
}
