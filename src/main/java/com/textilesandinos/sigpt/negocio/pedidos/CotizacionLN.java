package com.textilesandinos.sigpt.negocio.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.AdelantoDAO;
import com.textilesandinos.sigpt.datos.pedidos.CotizacionDAO;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.comun.TipoCambio;
import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;
import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.comun.TipoCambioLN;
import com.textilesandinos.sigpt.negocio.pedidos.estados.FabricaEstadoPedido;

/**
 * Cotizacion y adelanto del pedido (EF-1001-3, pasos 6 y 7).
 *
 * <p>La cotizacion tiene una vigencia; el adelanto verificado debe cubrir el
 * 40 % del valor pactado para que el pedido entre a produccion (Regla R2 /
 * RD2).
 */
@Service
public class CotizacionLN {

    private final CotizacionDAO cotizacionDAO;
    private final AdelantoDAO adelantoDAO;
    private final PedidoDAO pedidoDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final ParametrosNegocio parametros;
    private final TipoCambioLN tipoCambioLN;
    private final AuditoriaHelper auditoria;

    public CotizacionLN(CotizacionDAO cotizacionDAO, AdelantoDAO adelantoDAO,
            PedidoDAO pedidoDAO, OrdenProduccionDAO ordenDAO, ParametrosNegocio parametros,
            TipoCambioLN tipoCambioLN, AuditoriaHelper auditoria) {
        this.cotizacionDAO = cotizacionDAO;
        this.adelantoDAO = adelantoDAO;
        this.pedidoDAO = pedidoDAO;
        this.ordenDAO = ordenDAO;
        this.parametros = parametros;
        this.tipoCambioLN = tipoCambioLN;
        this.auditoria = auditoria;
    }

    /**
     * Emite la cotizacion del pedido (paso 6 del flujo de EF-1001-3).
     *
     * <p>La vigencia se calcula con el parametro VIGENCIA_COTIZACION_DIAS y el
     * pedido pasa de Registrado a Cotizado.
     */
    public ResultadoCotizacion emitir(CotizacionFormulario form, String codUsuario) {
        if (form == null || form.getIdPedido() == null) {
            throw new ReglaNegocioException("Debe seleccionar un pedido para cotizar.");
        }
        Pedido pedido = pedidoDAO.buscarPorId(form.getIdPedido())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        if ("ANULADO".equals(pedido.getEstado()) || "TERMINADO".equals(pedido.getEstado())) {
            throw new ReglaNegocioException("El pedido " + pedido.getCodPedido()
                    + " esta en estado " + pedido.getEstado() + ": no admite cotizacion.");
        }
        if (form.getMontoTotal() == null
                || form.getMontoTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto total pactado debe ser mayor que cero.");
        }

        String moneda = form.getMoneda() == null || form.getMoneda().isBlank()
                ? parametros.texto("MONEDA_PREDETERMINADA")
                : form.getMoneda();
        if (!"USD".equals(moneda) && !"PEN".equals(moneda)) {
            throw new ReglaNegocioException("La moneda " + moneda
                    + " no esta habilitada. Use USD o PEN.");
        }

        LocalDate emision = form.getFechaEmision() == null ? LocalDate.now()
                : form.getFechaEmision();
        int vigenciaDias = parametros.entero("VIGENCIA_COTIZACION_DIAS");

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdPedido(pedido.getIdPedido());
        cotizacion.setMontoTotal(form.getMontoTotal());
        cotizacion.setMoneda(moneda);
        cotizacion.setCostoInsumos(form.getCostoInsumos() == null ? BigDecimal.ZERO
                : form.getCostoInsumos());
        cotizacion.setHorasCiclo(form.getHorasCiclo() == null ? BigDecimal.ZERO
                : form.getHorasCiclo());
        cotizacion.setFechaEmision(emision);
        cotizacion.setFechaVigencia(emision.plusDays(vigenciaDias));
        cotizacion.setEstado("EMITIDA");

        int idCotizacion = cotizacionDAO.insertar(cotizacion, codUsuario);
        cotizacion.setIdCotizacion(idCotizacion);

        if ("REGISTRADO".equals(pedido.getEstado())) {
            transitar(pedido, "COTIZADO", codUsuario);
        }

        BigDecimal montoEnPen = tipoCambioLN.convertir(cotizacion.getMontoTotal(), moneda, "PEN",
                emision);
        TipoCambio vigente = tipoCambioLN.vigente(moneda, "PEN", emision);
        if (vigente == null) {
            vigente = tipoCambioLN.vigente("PEN", moneda, emision);
        }

        auditoria.registrar(codUsuario, "PEDIDOS", "EMITIR_COTIZACION", "cotizacion",
                String.valueOf(idCotizacion), "Pedido " + pedido.getCodPedido() + " | "
                        + cotizacion.getMontoTotal().toPlainString() + " " + moneda);

        ResultadoCotizacion resultado = new ResultadoCotizacion();
        resultado.setCotizacion(cotizacion);
        resultado.setMontoEnPen(monedaEnPen(cotizacion.getMontoTotal(), moneda, emision));
        resultado.setTipoCambioAplicado(vigente == null ? null : vigente.getValor());
        resultado.setMensaje("Cotizacion emitida con vigencia de " + vigenciaDias + " dias.");
        return resultado;
    }

    /**
     * Registra un adelanto sobre la cotizacion (paso 7 del flujo de EF-1001-3).
     *
     * <p>Cuando la suma de los adelantos verificados alcanza el porcentaje
     * minimo, el pedido pasa a En Produccion y se emite la Orden de Produccion
     * interna (Regla R2 / RD2).
     */
    public ResultadoAdelanto registrarAdelanto(AdelantoFormulario form, String codUsuario) {
        if (form == null || form.getIdCotizacion() == null) {
            throw new ReglaNegocioException("Debe seleccionar una cotizacion para registrar "
                    + "el adelanto.");
        }
        if (form.getMonto() == null || form.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto del adelanto debe ser mayor que cero.");
        }
        Cotizacion cotizacion = cotizacionDAO.buscarPorId(form.getIdCotizacion())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));

        LocalDate hoy = LocalDate.now();
        if (cotizacion.getFechaVigencia() != null && cotizacion.getFechaVigencia().isBefore(hoy)) {
            cotizacionDAO.actualizarEstado(cotizacion.getIdCotizacion(), "VENCIDA");
            throw new ReglaNegocioException("La cotizacion vencio el "
                    + cotizacion.getFechaVigencia() + " y no admite adelantos.");
        }
        if (!"EMITIDA".equals(cotizacion.getEstado())
                && !"ACEPTADA".equals(cotizacion.getEstado())) {
            throw new ReglaNegocioException("La cotizacion se encuentra en estado "
                    + cotizacion.getEstado() + ": no admite adelantos.");
        }

        Pedido pedido = pedidoDAO.buscarPorId(cotizacion.getIdPedido())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));

        Adelanto adelanto = new Adelanto();
        adelanto.setIdCotizacion(cotizacion.getIdCotizacion());
        adelanto.setMonto(form.getMonto());
        adelanto.setMoneda(cotizacion.getMoneda());
        adelanto.setFechaAdelanto(form.getFechaAdelanto() == null ? hoy
                : form.getFechaAdelanto());
        adelanto.setVerificado(form.isVerificado());
        adelanto.setReferencia(form.getReferencia());
        int idAdelanto = adelantoDAO.insertar(adelanto, codUsuario);
        adelanto.setIdAdelanto(idAdelanto);

        BigDecimal verificado = adelantoDAO.sumaVerificada(cotizacion.getIdCotizacion());
        BigDecimal porcentaje = BigDecimal.ZERO;
        if (cotizacion.getMontoTotal() != null
                && cotizacion.getMontoTotal().compareTo(BigDecimal.ZERO) > 0) {
            porcentaje = verificado.multiply(BigDecimal.valueOf(100))
                    .divide(cotizacion.getMontoTotal(), 2, RoundingMode.HALF_UP);
        }
        BigDecimal minimo = parametros.decimal("ADELANTO_MINIMO_PORCENTAJE");
        boolean habilitada = porcentaje.compareTo(minimo) >= 0;

        ResultadoAdelanto resultado = new ResultadoAdelanto();
        resultado.setAdelanto(adelanto);
        resultado.setPorcentajeCubierto(porcentaje);
        resultado.setProduccionHabilitada(habilitada);

        if (habilitada && "COTIZADO".equals(pedido.getEstado())) {
            transitar(pedido, "EN_PRODUCCION", codUsuario);
            resultado.setOrdenProduccion(emiteOrdenProduccion(pedido, cotizacion, codUsuario));
            resultado.setMensaje("Adelanto verificado: " + porcentaje.toPlainString()
                    + " % del valor pactado. La Orden de Produccion "
                    + resultado.getOrdenProduccion().getCodOrden() + " fue emitida a planta.");
        } else {
            resultado.setMensaje("Adelanto registrado.lleva " + porcentaje.toPlainString()
                    + " % del valor pactado (se requieren " + minimo.toPlainString() + " %).");
        }

        auditoria.registrar(codUsuario, "PEDIDOS", "REGISTRAR_ADELANTO", "adelanto",
                String.valueOf(idAdelanto), "Cotizacion " + cotizacion.getIdCotizacion() + " | "
                        + form.getMonto().toPlainString() + " " + cotizacion.getMoneda()
                        + " | " + (form.isVerificado() ? "verificado" : "sin verificar"));
        return resultado;
    }

    /** Lista todas las cotizaciones emitidas, con su pedido y su adelanto. */
    public List<Cotizacion> listarCotizaciones() {
        cotizacionDAO.marcarVencidas(LocalDate.now());
        return cotizacionDAO.listarTodas();
    }

    private OrdenProduccion emiteOrdenProduccion(Pedido pedido, Cotizacion cotizacion,
            String codUsuario) {
        OrdenProduccion orden = new OrdenProduccion();
        orden.setCodOrden(ordenDAO.siguienteCodigo());
        orden.setIdPedido(pedido.getIdPedido());
        orden.setMetrosTotales(pedido.getMetros());
        orden.setHorasMaquinaPrevistas(cotizacion.getHorasCiclo());
        // Supuesto documentado: un telar por turno, con un operario por telar.
        orden.setHorasHombrePrevistas(cotizacion.getHorasCiclo());
        orden.setFechaInicio(LocalDate.now());
        orden.setEstado("EN_PRODUCCION");
        int idOrden = ordenDAO.insertar(orden, codUsuario);
        orden.setIdOrden(idOrden);
        auditoria.registrar(codUsuario, "PEDIDOS", "EMITIR_ORDEN_PRODUCCION", "orden_produccion",
                String.valueOf(idOrden), "Pedido " + pedido.getCodPedido() + " | "
                        + orden.getCodOrden());
        return orden;
    }

    private void transitar(Pedido pedido, String destino, String codUsuario) {
        if (!FabricaEstadoPedido.puedeTransitar(pedido.getEstado(), destino)) {
            throw new ReglaNegocioException("El pedido " + pedido.getCodPedido()
                    + " no puede pasar de " + pedido.getEstado() + " a " + destino + ".");
        }
        pedidoDAO.actualizarEstado(pedido.getIdPedido(), destino, codUsuario);
    }

    /** Convierte el monto pactado a soles para la vista del jefe de produccion. */
    private BigDecimal monedaEnPen(BigDecimal monto, String moneda, LocalDate fecha) {
        try {
            return tipoCambioLN.convertir(monto, moneda, "PEN", fecha);
        } catch (ReglaNegocioException e) {
            return BigDecimal.ZERO;
        }
    }
}
