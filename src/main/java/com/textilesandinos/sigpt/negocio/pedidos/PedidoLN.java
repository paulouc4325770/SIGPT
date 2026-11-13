package com.textilesandinos.sigpt.negocio.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.AdelantoDAO;
import com.textilesandinos.sigpt.datos.pedidos.ClienteDAO;
import com.textilesandinos.sigpt.datos.pedidos.CotizacionDAO;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoDetalle;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.estados.FabricaEstadoPedido;
import com.textilesandinos.sigpt.negocio.planta.CapacidadProduccionLN;

/**
 * Reglas del pedido textil (EF-1001-3).
 *
 * <p>Aplica el metraje minimo (Regla R1 / RD1), evalua la viabilidad operativa
 * y controla el ciclo de vida del pedido con el patron State.
 */
@Service
public class PedidoLN {

    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;
    private final CotizacionDAO cotizacionDAO;
    private final AdelantoDAO adelantoDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final CapacidadProduccionLN capacidad;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;

    public PedidoLN(PedidoDAO pedidoDAO, ClienteDAO clienteDAO, CotizacionDAO cotizacionDAO,
            AdelantoDAO adelantoDAO, OrdenProduccionDAO ordenDAO, CapacidadProduccionLN capacidad,
            ParametrosNegocio parametros, AuditoriaHelper auditoria) {
        this.pedidoDAO = pedidoDAO;
        this.clienteDAO = clienteDAO;
        this.cotizacionDAO = cotizacionDAO;
        this.adelantoDAO = adelantoDAO;
        this.ordenDAO = ordenDAO;
        this.capacidad = capacidad;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    /** Lista los pedidos que cumplen los criterios de busqueda. */
    public List<PedidoFila> listar(String codigo, String estado, Integer idCliente,
            LocalDate desde, LocalDate hasta) {
        return pedidoDAO.listar(codigo, estado, idCliente, desde, hasta);
    }

    /** Tablero de control de la bandeja de pedidos. */
    public ResumenPedidos resumir() {
        return pedidoDAO.resumir();
    }

    /**
     * Tablero de control calculado sobre los pedidos que se estan mostrando.
     *
     * <p>Si el usuario filtra por estado, cliente o fechas, el semaforo
     * acompana al filtro: no puede decir "3 pedidos" mientras la tabla esta
     * vacia.
     */
    public ResumenPedidos resumir(List<PedidoFila> pedidos) {
        ResumenPedidos resumen = new ResumenPedidos();
        for (PedidoFila pedido : pedidos) {
            switch (pedido.getEstado() == null ? "" : pedido.getEstado()) {
                case "REGISTRADO" -> resumen.setRegistrados(resumen.getRegistrados() + 1);
                case "COTIZADO" -> resumen.setCotizados(resumen.getCotizados() + 1);
                case "EN_PRODUCCION" -> resumen.setEnProduccion(resumen.getEnProduccion() + 1);
                case "TERMINADO" -> resumen.setTerminados(resumen.getTerminados() + 1);
                case "ANULADO" -> resumen.setAnulados(resumen.getAnulados() + 1);
                default -> { }
            }
        }
        resumen.setTotal(pedidos.size());
        return resumen;
    }

    /**
     * Registra un pedido con su ficha tecnica (flujo 1 a 5 de EF-1001-3).
     *
     * @return identificador del pedido generado
     */
    public int registrar(PedidoFormulario form, String codUsuario) {
        validarCampos(form);

        BigDecimal minimo = parametros.decimal("PEDIDO_MINIMO_METROS");
        if (form.getMetros().compareTo(minimo) < 0) {
            throw new ReglaNegocioException("El metraje minimo de manufactura y teñido es de "
                    + String.format("%,d", minimo.longValue()) + " metros (Regla R1).", "RD1");
        }

        Cliente cliente = clienteDAO.buscarPorId(form.getIdCliente())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El cliente seleccionado no esta registrado."));
        if (!"ACTIVO".equals(cliente.getEstado())) {
            throw new ReglaNegocioException("El cliente " + cliente.getRazonSocial()
                    + " esta inactivo: actualice su registro antes de cotizar.");
        }

        if (!capacidad.esViable(form.getMetros(), form.getFechaEntrega())) {
            BigDecimal capacidadPeriodo = capacidad.capacidadDiariaMetros()
                    .multiply(BigDecimal.valueOf(capacidad.diasDisponibles(form.getFechaEntrega())));
            throw new ReglaNegocioException("El pedido no es viable: la capacidad disponible para "
                    + "el periodo solicitado es de " + capacidadPeriodo.toPlainString()
                    + " m y el pedido requiere " + form.getMetros().toPlainString() + " m.");
        }

        Pedido pedido = new Pedido();
        pedido.setCodPedido(pedidoDAO.siguienteCodigo());
        pedido.setIdCliente(form.getIdCliente());
        pedido.setMetros(form.getMetros());
        pedido.setFechaEntrega(form.getFechaEntrega());
        pedido.setEstado(FabricaEstadoPedido.de("REGISTRADO").codigo());

        FichaTecnica ficha = new FichaTecnica();
        ficha.setTipoHilado(form.getTipoHilado());
        ficha.setGramajeM2(form.getGramajeM2());
        ficha.setColorPantone(form.getColorPantone());
        ficha.setComposicion(form.getComposicion());
        ficha.setMetros(form.getMetros());
        ficha.setToleranciaColor(form.getToleranciaColor() == null
                ? parametros.decimal("TOLERANCIA_COLOR_DEFAULT")
                : form.getToleranciaColor());
        ficha.setObservaciones(form.getObservaciones());

        int idPedido = pedidoDAO.insertarConFicha(pedido, ficha, codUsuario);
        auditoria.registrar(codUsuario, "PEDIDOS", "REGISTRAR_PEDIDO", "pedido",
                String.valueOf(idPedido),
                "Codigo " + pedido.getCodPedido() + " | " + form.getMetros().toPlainString()
                        + " m | Cliente " + cliente.getRazonSocial());
        return idPedido;
    }

    /** Ficha completa del pedido: datos, cotizaciones, adelantos y orden de produccion. */
    public PedidoDetalle consultar(int idPedido) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setCliente(clienteDAO.buscarPorId(pedido.getIdCliente()).orElse(null));
        detalle.setFichaTecnica(pedidoDAO.buscarFichaTecnica(idPedido).orElse(null));
        List<Cotizacion> cotizaciones = cotizacionDAO.listarPorPedido(idPedido);
        detalle.setCotizaciones(cotizaciones);
        detalle.setOrdenProduccion(ordenDAO.buscarPorPedido(idPedido).orElse(null));

        if (!cotizaciones.isEmpty()) {
            Cotizacion vigente = cotizaciones.get(0);
            List<Adelanto> adelantos = adelantoDAO.listarPorCotizacion(vigente.getIdCotizacion());
            detalle.setAdelantos(adelantos);
            detalle.setMontoPactado(vigente.getMontoTotal());
            BigDecimal verificado = adelantoDAO.sumaVerificada(vigente.getIdCotizacion());
            detalle.setAdelantoVerificado(verificado);
            if (vigente.getMontoTotal() != null
                    && vigente.getMontoTotal().compareTo(BigDecimal.ZERO) > 0) {
                detalle.setPorcentajeAdelanto(verificado.multiply(BigDecimal.valueOf(100))
                        .divide(vigente.getMontoTotal(), 2, RoundingMode.HALF_UP));
            }
        }
        return detalle;
    }

    /**
     * Anula un pedido que aun no ha entrado a planta.
     *
     * <p>La transicion la valida el patron State: un pedido en produccion no se
     * anula, lo que se anula es la orden de produccion.
     */
    public void anular(int idPedido, String motivo, String codUsuario) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        if (!FabricaEstadoPedido.puedeTransitar(pedido.getEstado(), "ANULADO")) {
            throw new ReglaNegocioException("El pedido " + pedido.getCodPedido()
                    + " no puede anularse en estado " + pedido.getEstado() + ".");
        }
        pedidoDAO.actualizarEstado(idPedido, "ANULADO", codUsuario);
        auditoria.registrar(codUsuario, "PEDIDOS", "ANULAR_PEDIDO", "pedido",
                String.valueOf(idPedido),
                "Codigo " + pedido.getCodPedido() + " | Motivo: "
                        + (motivo == null || motivo.isBlank() ? "sin motivo" : motivo));
    }

    private void validarCampos(PedidoFormulario form) {
        if (form == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (form.getIdCliente() == null) {
            throw new ReglaNegocioException("Debe seleccionar un cliente.");
        }
        if (form.getTipoHilado() == null || form.getTipoHilado().isBlank()) {
            throw new ReglaNegocioException("El tipo de hilado es obligatorio.");
        }
        if (form.getGramajeM2() == null || form.getGramajeM2().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El gramaje por m2 es obligatorio.");
        }
        if (form.getColorPantone() == null || form.getColorPantone().isBlank()) {
            throw new ReglaNegocioException("El color Pantone es obligatorio.");
        }
        if (form.getComposicion() == null || form.getComposicion().isBlank()) {
            throw new ReglaNegocioException("La composicion de la mezcla es obligatoria.");
        }
        if (form.getMetros() == null || form.getMetros().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad de metros es obligatoria.");
        }
        if (form.getFechaEntrega() == null) {
            throw new ReglaNegocioException("La fecha de entrega es obligatoria.");
        }
    }
}
