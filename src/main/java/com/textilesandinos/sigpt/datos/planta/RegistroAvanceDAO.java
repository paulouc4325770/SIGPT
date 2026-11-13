package com.textilesandinos.sigpt.datos.planta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;

/**
 * Acceso a los registros de avance de produccion.
 */
public interface RegistroAvanceDAO {

    /** Inserta el avance y devuelve su identificador. */
    int insertar(RegistroAvance avance, String codUsuario);

    /** Busca un avance por su identificador. */
    Optional<RegistroAvance> buscarPorId(int idAvance);

    /** Lista los avances de una orden de produccion. */
    List<RegistroAvance> listarPorOrden(int idOrdenProduccion);

    /** Lista los avances registrados entre dos fechas (cualquiera puede ser nula). */
    List<RegistroAvance> listarEntreFechas(LocalDate desde, LocalDate hasta);

    /** Lista los avances de una asignacion, del mas reciente al mas antiguo. */
    List<RegistroAvance> listarPorAsignacion(int idAsignacion);

    /** Actualiza el estado de calidad del avance (PENDIENTE, CONFORME, NO_CONFORME). */
    int actualizarEstadoCalidad(int idAvance, String estadoCalidad);
}
