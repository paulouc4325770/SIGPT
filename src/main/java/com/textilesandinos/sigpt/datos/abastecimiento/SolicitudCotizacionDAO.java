package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.abastecimiento.SolicitudCotizacion;

/**
 * Acceso a las solicitudes de cotizacion a proveedores (RFQ).
 */
public interface SolicitudCotizacionDAO {

    /** Inserta la solicitud y devuelve su identificador. */
    int insertar(SolicitudCotizacion solicitud, String codUsuario);

    /** Lista las solicitudes, de la mas reciente a la mas antigua. */
    List<SolicitudCotizacion> listarTodas();

    /** Lista las solicitudes de una orden de produccion. */
    List<SolicitudCotizacion> listarPorOrden(int idOrdenProduccion);

    /** Busca una solicitud por su identificador. */
    Optional<SolicitudCotizacion> buscarPorId(int idSolicitud);

    /** Cambia el estado de la solicitud (EMITIDA, CERRADA, CANCELADA). */
    int actualizarEstado(int idSolicitud, String estado);

    /** Genera el siguiente codigo de solicitud (RFQ-AAAA-NNN). */
    String siguienteCodigo();
}
