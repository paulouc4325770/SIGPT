package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.abastecimiento.CotizacionProveedorDAO;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.SolicitudCotizacionDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.CotizacionProveedor;
import com.textilesandinos.sigpt.entidades.abastecimiento.SolicitudCotizacion;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Solicitud de cotizacion a proveedores (RFQ) y su adjudicacion.
 *
 * <p>El flujo es: el Jefe de Logistica emite una solicitud asociada a una Orden
 * de Produccion, los proveedores cotizan y el Jefe de Logistica adjudica la
 * mejor oferta segun la matriz costo, tiempo de entrega y certificaciones.
 */
@Service
public class CotizacionProveedorLN {

    private final SolicitudCotizacionDAO solicitudDAO;
    private final CotizacionProveedorDAO cotizacionDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final AuditoriaHelper auditoria;

    public CotizacionProveedorLN(SolicitudCotizacionDAO solicitudDAO,
            CotizacionProveedorDAO cotizacionDAO, OrdenProduccionDAO ordenDAO,
            AuditoriaHelper auditoria) {
        this.solicitudDAO = solicitudDAO;
        this.cotizacionDAO = cotizacionDAO;
        this.ordenDAO = ordenDAO;
        this.auditoria = auditoria;
    }

    /** Lista las solicitudes de cotizacion emitidas. */
    public List<SolicitudCotizacion> listarSolicitudes() {
        return solicitudDAO.listarTodas();
    }

    /** Cotizaciones recibidas de una solicitud. */
    public List<CotizacionProveedor> cotizacionesDe(int idSolicitud) {
        return cotizacionDAO.listarPorSolicitud(idSolicitud);
    }

    /**
     * Emite la solicitud de cotizacion (RFQ) de una Orden de Produccion.
     */
    public SolicitudCotizacion emitirSolicitud(SolicitudCotizacionFormulario formulario,
            String codUsuario) {
        if (formulario == null || formulario.getIdOrdenProduccion() == null) {
            throw new ReglaNegocioException("Debe seleccionar la Orden de Produccion que "
                    + "originia la solicitud.");
        }
        var orden = ordenDAO.buscarPorId(formulario.getIdOrdenProduccion())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        if (!"EN_PRODUCCION".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La Orden de Produccion " + orden.getCodOrden()
                    + " no esta en produccion: no admite solicitudes de compra.");
        }

        SolicitudCotizacion solicitud = new SolicitudCotizacion();
        solicitud.setCodigo(solicitudDAO.siguienteCodigo());
        solicitud.setIdOrdenProduccion(formulario.getIdOrdenProduccion());
        solicitud.setFechaEmision(formulario.getFechaEmision() == null ? LocalDate.now()
                : formulario.getFechaEmision());
        solicitud.setEstado("EMITIDA");
        solicitud.setObservaciones(formulario.getObservaciones());

        int idSolicitud = solicitudDAO.insertar(solicitud, codUsuario);
        solicitud.setIdSolicitud(idSolicitud);

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "EMITIR_SOLICITUD_COTIZACION",
                "solicitud_cotizacion", String.valueOf(idSolicitud),
                solicitud.getCodigo() + " | Orden " + orden.getCodOrden());
        return solicitud;
    }

    /** Registra la cotizacion enviada por un proveedor. */
    public CotizacionProveedor registrarCotizacion(CotizacionProveedorFormulario formulario,
            String codUsuario) {
        if (formulario == null || formulario.getIdSolicitud() == null
                || formulario.getIdProveedor() == null) {
            throw new ReglaNegocioException("Debe indicar la solicitud y el proveedor.");
        }
        if (formulario.getPrecioUnitario() == null
                || formulario.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio unitario cotizado debe ser mayor que "
                    + "cero.");
        }
        CotizacionProveedor cotizacion = new CotizacionProveedor();
        cotizacion.setIdSolicitud(formulario.getIdSolicitud());
        cotizacion.setIdProveedor(formulario.getIdProveedor());
        cotizacion.setPrecioUnitario(formulario.getPrecioUnitario());
        cotizacion.setUnidad(formulario.getUnidad() == null ? "KG" : formulario.getUnidad());
        cotizacion.setTiempoEntregaDias(formulario.getTiempoEntregaDias() == null ? 0
                : formulario.getTiempoEntregaDias());
        cotizacion.setCertificaciones(formulario.getCertificaciones());
        cotizacion.setObservaciones(formulario.getObservaciones());
        cotizacion.setEstado("RECIBIDA");

        int idCotizacion = cotizacionDAO.insertar(cotizacion, codUsuario);
        cotizacion.setIdCotizacionProveedor(idCotizacion);

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "REGISTRAR_COTIZACION_PROVEEDOR",
                "cotizacion_proveedor", String.valueOf(idCotizacion),
                "Precio " + formulario.getPrecioUnitario().toPlainString() + " | Entrega "
                        + cotizacion.getTiempoEntregaDias() + " dias");
        return cotizacion;
    }

    /**
     * Adjudica la mejor oferta de la solicitud y cierra la solicitud.
     *
     * <p>La comparacion usa el puntaje de la matriz costo-beneficio-tiempo: el
     * precio mas bajo suma, el tiempo de entrega mas corto suma y tener
     * certificaciones suma.
     */
    public CotizacionProveedor adjudicar(int idSolicitud, String codUsuario) {
        List<CotizacionProveedor> cotizaciones = cotizacionDAO.listarPorSolicitud(idSolicitud);
        if (cotizaciones.isEmpty()) {
            throw new ReglaNegocioException("La solicitud no tiene cotizaciones recibidas: "
                    + "no se puede adjudicar.");
        }
        CotizacionProveedor mejor = null;
        BigDecimal mejorPuntaje = null;
        for (CotizacionProveedor cotizacion : cotizaciones) {
            BigDecimal puntaje = puntuar(cotizacion, cotizaciones);
            if (mejorPuntaje == null || puntaje.compareTo(mejorPuntaje) > 0) {
                mejorPuntaje = puntaje;
                mejor = cotizacion;
            }
        }
        for (CotizacionProveedor cotizacion : cotizaciones) {
            cotizacionDAO.actualizarEstado(cotizacion.getIdCotizacionProveedor(),
                    cotizacion.getIdCotizacionProveedor().equals(mejor.getIdCotizacionProveedor())
                            ? "ADJUDICADA" : "DESCARTADA");
        }
        solicitudDAO.actualizarEstado(idSolicitud, "CERRADA");

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "ADJUDICAR_COTIZACION",
                "cotizacion_proveedor",
                String.valueOf(mejor.getIdCotizacionProveedor()),
                "Adjudicada por precio, tiempo y certificaciones");
        return mejor;
    }

    /**
     * Puntaje de la oferta: menor precio, menor tiempo de entrega y
     * certificaciones suman puntos.
     */
    private BigDecimal puntuar(CotizacionProveedor cotizacion,
            List<CotizacionProveedor> cotizaciones) {
        BigDecimal menorPrecio = cotizaciones.stream()
                .map(CotizacionProveedor::getPrecioUnitario)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        BigDecimal menorTiempo = BigDecimal.valueOf(cotizaciones.stream()
                .map(CotizacionProveedor::getTiempoEntregaDias)
                .min(Integer::compareTo)
                .orElse(0));

        BigDecimal puntos = BigDecimal.ZERO;
        if (menorPrecio.compareTo(BigDecimal.ZERO) > 0
                && cotizacion.getPrecioUnitario().compareTo(menorPrecio) == 0) {
            puntos = puntos.add(BigDecimal.valueOf(60));
        }
        if (menorTiempo.compareTo(BigDecimal.ZERO) > 0
                && cotizacion.getTiempoEntregaDias() == menorTiempo.intValue()) {
            puntos = puntos.add(BigDecimal.valueOf(25));
        }
        if (cotizacion.getCertificaciones() != null
                && !cotizacion.getCertificaciones().isBlank()) {
            puntos = puntos.add(BigDecimal.valueOf(15));
        }
        return puntos.setScale(2, RoundingMode.HALF_UP);
    }
}
