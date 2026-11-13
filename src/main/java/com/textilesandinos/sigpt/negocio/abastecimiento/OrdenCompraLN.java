package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.abastecimiento.InsumoDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.OrdenCompraDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.ProveedorDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompraDetalle;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utiles.documento.GeneradorOrdenCompraPdf;
import com.textilesandinos.sigpt.utiles.notificacion.NotificadorOrdenCompra;

/**
 * Orden de compra al proveedor (EF-1001-5).
 *
 * <p>Al emitir la orden se genera su PDF, se guarda en el servidor y se envia
 * al proveedor mediante el adaptador de notificacion. Al recibir la mercaderia
 * se registran los ingresos en el kardex y el stock de los insumos queda
 * actualizado.
 */
@Service
public class OrdenCompraLN {

    private final OrdenCompraDAO ordenDAO;
    private final ProveedorDAO proveedorDAO;
    private final InsumoDAO insumoDAO;
    private final KardexLN kardexLN;
    private final GeneradorOrdenCompraPdf generadorPdf;
    private final NotificadorOrdenCompra notificador;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;
    private final String carpetaOrdenes;

    public OrdenCompraLN(OrdenCompraDAO ordenDAO, ProveedorDAO proveedorDAO, InsumoDAO insumoDAO,
            KardexLN kardexLN, GeneradorOrdenCompraPdf generadorPdf,
            NotificadorOrdenCompra notificador, ParametrosNegocio parametros,
            AuditoriaHelper auditoria,
            @Value("${sigpt.ordenes-compra.carpeta:./data/ordenes-compra}") String carpetaOrdenes) {
        this.ordenDAO = ordenDAO;
        this.proveedorDAO = proveedorDAO;
        this.insumoDAO = insumoDAO;
        this.kardexLN = kardexLN;
        this.generadorPdf = generadorPdf;
        this.notificador = notificador;
        this.parametros = parametros;
        this.auditoria = auditoria;
        this.carpetaOrdenes = carpetaOrdenes;
    }

    /** Lista las ordenes de compra. */
    public List<OrdenCompra> listar() {
        return ordenDAO.listarTodas();
    }

    /** Detalle de una orden de compra. */
    public List<OrdenCompraDetalle> detalle(int idOrdenCompra) {
        return ordenDAO.listarDetalle(idOrdenCompra);
    }

    /**
     * Emite la orden de compra, genera su PDF y la envia al proveedor.
     *
     * @return la orden emitida
     */
    public OrdenCompra emitir(OrdenCompraFormulario formulario, String codUsuario) {
        if (formulario == null || formulario.getIdProveedor() == null) {
            throw new ReglaNegocioException("Debe seleccionar el proveedor de la orden.");
        }
        if (formulario.getIdInsumo() == null || formulario.getCantidad() == null
                || formulario.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La orden debe tener al menos un insumo con "
                    + "cantidad mayor que cero.");
        }
        Proveedor proveedor = proveedorDAO.buscarPorId(formulario.getIdProveedor())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El proveedor seleccionado no esta registrado."));
        if (!"ACTIVO".equals(proveedor.getEstado())) {
            throw new ReglaNegocioException("El proveedor " + proveedor.getCodigo()
                    + " esta inactivo: no se le pueden emitir ordenes.");
        }
        Insumo insumo = insumoDAO.buscarPorId(formulario.getIdInsumo())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El insumo seleccionado no esta registrado en el catalogo."));

        BigDecimal precio = formulario.getPrecioUnitario() != null ? formulario.getPrecioUnitario()
                : insumo.getPrecioUnitario();
        BigDecimal monto = formulario.getCantidad().multiply(precio);

        OrdenCompra orden = new OrdenCompra();
        orden.setCodigo(ordenDAO.siguienteCodigo());
        orden.setIdProveedor(formulario.getIdProveedor());
        orden.setIdSolicitud(formulario.getIdSolicitud());
        orden.setIdCotizacionProveedor(formulario.getIdCotizacionProveedor());
        orden.setMontoTotal(monto);
        orden.setMoneda(formulario.getMoneda() == null || formulario.getMoneda().isBlank()
                ? parametros.texto("MONEDA_PREDETERMINADA") : formulario.getMoneda());
        orden.setFechaEmision(formulario.getFechaEmision() == null ? LocalDate.now()
                : formulario.getFechaEmision());
        orden.setFechaEntregaEsperada(formulario.getFechaEntregaEsperada());
        orden.setEstado("EMITIDA");
        orden.setObservaciones(formulario.getObservaciones());

        List<OrdenCompraDetalle> detalle = new ArrayList<>();
        OrdenCompraDetalle linea = new OrdenCompraDetalle();
        linea.setIdInsumo(insumo.getIdInsumo());
        linea.setCantidad(formulario.getCantidad());
        linea.setPrecioUnitario(precio);
        detalle.add(linea);

        int idOrden = ordenDAO.insertarConDetalle(orden, detalle, codUsuario);
        orden.setIdOrdenCompra(idOrden);

        // PDF y envio al proveedor (adaptador).
        byte[] pdf = generadorPdf.generar(orden, proveedor, detalle, insumoDAO.listarTodos());
        String ruta = guardarPdf(orden.getCodigo(), pdf);
        ordenDAO.actualizarRutaPdf(idOrden, ruta);
        orden.setRutaPdf(ruta);

        NotificadorOrdenCompra.ResultadoEnvio envio =
                notificador.enviar(orden, proveedor, pdf, ruta);
        if (!envio.exitoso()) {
            auditoria.registrar(codUsuario, "ABASTECIMIENTO", "ENVIAR_ORDEN_COMPRA", "orden_compra",
                    String.valueOf(idOrden), "El envio al proveedor fallo: " + envio.detalle());
        }

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "EMITIR_ORDEN_COMPRA", "orden_compra",
                String.valueOf(idOrden), orden.getCodigo() + " | " + proveedor.getRazonSocial()
                        + " | " + monto.toPlainString() + " " + orden.getMoneda()
                        + " | PDF: " + ruta);
        return orden;
    }

    /** Confirma la orden de compra con el proveedor. */
    public void confirmar(int idOrdenCompra, String codUsuario) {
        OrdenCompra orden = buscar(idOrdenCompra);
        if (!"EMITIDA".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La orden " + orden.getCodigo()
                    + " no se puede confirmar en estado " + orden.getEstado() + ".");
        }
        ordenDAO.actualizarEstado(idOrdenCompra, "CONFIRMADA");
        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "CONFIRMAR_ORDEN_COMPRA", "orden_compra",
                String.valueOf(idOrdenCompra), orden.getCodigo());
    }

    /**
     * Registra la recepcion de la mercaderia: genera los ingresos de kardex y
     * actualiza el stock de los insumos.
     */
    public void recibir(int idOrdenCompra, String codUsuario) {
        OrdenCompra orden = buscar(idOrdenCompra);
        if ("RECIBIDA".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La orden " + orden.getCodigo()
                    + " ya fue recibida.");
        }
        if ("ANULADA".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La orden " + orden.getCodigo()
                    + " esta anulada: no admite recepcion.");
        }
        for (OrdenCompraDetalle linea : ordenDAO.listarDetalle(idOrdenCompra)) {
            KardexFormulario movimiento = new KardexFormulario();
            movimiento.setIdInsumo(linea.getIdInsumo());
            movimiento.setTipoMovimiento("INGRESO");
            movimiento.setCantidad(linea.getCantidad());
            movimiento.setPrecioUnitario(linea.getPrecioUnitario());
            movimiento.setIdOrdenCompra(idOrdenCompra);
            movimiento.setFecha(orden.getFechaEmision());
            movimiento.setObservaciones("Recepcion de la orden " + orden.getCodigo());
            kardexLN.registrar(movimiento, codUsuario);
        }
        ordenDAO.actualizarEstado(idOrdenCompra, "RECIBIDA");
        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "RECIBIR_ORDEN_COMPRA", "orden_compra",
                String.valueOf(idOrdenCompra), orden.getCodigo() + " | Ingresos al kardex");
    }

    /** Anula la orden de compra. */
    public void anular(int idOrdenCompra, String motivo, String codUsuario) {
        OrdenCompra orden = buscar(idOrdenCompra);
        if ("RECIBIDA".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La orden " + orden.getCodigo()
                    + " ya fue recibida: no puede anularse.");
        }
        ordenDAO.actualizarEstado(idOrdenCompra, "ANULADA");
        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "ANULAR_ORDEN_COMPRA", "orden_compra",
                String.valueOf(idOrdenCompra),
                orden.getCodigo() + " | Motivo: " + (motivo == null ? "sin motivo" : motivo));
    }

    /** Regenera el PDF de una orden de compra. */
    public byte[] pdf(int idOrdenCompra) {
        OrdenCompra orden = buscar(idOrdenCompra);
        Proveedor proveedor = proveedorDAO.buscarPorId(orden.getIdProveedor())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        return generadorPdf.generar(orden, proveedor, ordenDAO.listarDetalle(idOrdenCompra),
                insumoDAO.listarTodos());
    }

    private OrdenCompra buscar(int idOrdenCompra) {
        return ordenDAO.buscarPorId(idOrdenCompra)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
    }

    private String guardarPdf(String codigo, byte[] pdf) {
        try {
            java.nio.file.Path carpeta = java.nio.file.Path.of(carpetaOrdenes);
            java.nio.file.Files.createDirectories(carpeta);
            java.nio.file.Path destino = carpeta.resolve(codigo + ".pdf");
            java.nio.file.Files.write(destino, pdf);
            return destino.toString();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo guardar el PDF de la orden " + codigo, e);
        }
    }
}
