package com.textilesandinos.sigpt.datos.planta;

import java.util.List;

import com.textilesandinos.sigpt.entidades.planta.InspeccionCalidad;

/**
 * Acceso a las inspecciones de calidad de los lotes.
 */
public interface InspeccionCalidadDAO {

    /** Inserta la inspeccion y devuelve su identificador. */
    int insertar(InspeccionCalidad inspeccion, String codUsuario);

    /** Inspecciones registradas de un lote. */
    List<InspeccionCalidad> listarPorLote(int idLote);
}
