package com.textilesandinos.sigpt.datos.planta;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.Lote;

/**
 * Acceso a los lotes de produccion.
 */
public interface LoteDAO {

    /** Inserta el lote y devuelve su identificador. */
    int insertar(Lote lote, String codUsuario);

    /** Busca un lote por su identificador. */
    Optional<Lote> buscarPorId(int idLote);

    /** Lotes que esperan inspeccion de calidad. */
    List<Lote> listarPendientes();

    /** Lotes de una orden de produccion. */
    List<Lote> listarPorOrden(int idOrdenProduccion);

    /** Cambia el estado del lote (PENDIENTE_INSPECCION, CONFORME, MERMA). */
    int actualizarEstado(int idLote, String estado);

    /** Genera el siguiente codigo de lote (LOTE-AAAA-NNNN). */
    String siguienteCodigo();
}
