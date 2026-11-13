package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.abastecimiento.CotizacionProveedor;

/**
 * Acceso a las cotizaciones recibidas de los proveedores.
 */
public interface CotizacionProveedorDAO {

    /** Inserta la cotizacion del proveedor y devuelve su identificador. */
    int insertar(CotizacionProveedor cotizacion, String codUsuario);

    /** Lista las cotizaciones de una solicitud. */
    List<CotizacionProveedor> listarPorSolicitud(int idSolicitud);

    /** Busca una cotizacion por su identificador. */
    Optional<CotizacionProveedor> buscarPorId(int idCotizacionProveedor);

    /** Cambia el estado de la cotizacion (RECIBIDA, ADJUDICADA, DESCARTADA). */
    int actualizarEstado(int idCotizacionProveedor, String estado);
}
