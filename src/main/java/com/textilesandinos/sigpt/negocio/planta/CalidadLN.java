package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.InspeccionCalidadDAO;
import com.textilesandinos.sigpt.datos.planta.LoteDAO;
import com.textilesandinos.sigpt.datos.planta.ParteDiarioDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.InspeccionCalidad;
import com.textilesandinos.sigpt.entidades.planta.Lote;
import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.estados.FabricaEstadoPedido;

/**
 * Control de calidad y cierre de lote (EF-1001-7, pasos 6 a 11).
 *
 * <p>Aplica la Regla R-Merma: un lote no conforme se registra como Merma y
 * debe reprocesarse, y el progreso del pedido no avanza. La Regla RD7 toma la
 * tolerancia de varianza de color de la ficha tecnica del pedido.
 */
@Service
public class CalidadLN {

    private final LoteDAO loteDAO;
    private final InspeccionCalidadDAO inspeccionDAO;
    private final RegistroAvanceDAO avanceDAO;
    private final AsignacionDAO asignacionDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final PedidoDAO pedidoDAO;
    private final ParteDiarioDAO parteDiarioDAO;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;

    public CalidadLN(LoteDAO loteDAO, InspeccionCalidadDAO inspeccionDAO,
            RegistroAvanceDAO avanceDAO, AsignacionDAO asignacionDAO, OrdenProduccionDAO ordenDAO,
            PedidoDAO pedidoDAO, ParteDiarioDAO parteDiarioDAO, ParametrosNegocio parametros,
            AuditoriaHelper auditoria) {
        this.loteDAO = loteDAO;
        this.inspeccionDAO = inspeccionDAO;
        this.avanceDAO = avanceDAO;
        this.asignacionDAO = asignacionDAO;
        this.ordenDAO = ordenDAO;
        this.pedidoDAO = pedidoDAO;
        this.parteDiarioDAO = parteDiarioDAO;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    /** Lotes que esperan inspeccion de calidad. */
    public List<Lote> lotesPendientes() {
        return loteDAO.listarPendientes();
    }

    /** Lotes de una orden de produccion. */
    public List<Lote> lotesDe(int idOrdenProduccion) {
        return loteDAO.listarPorOrden(idOrdenProduccion);
    }

    /** Partes diarios de una orden. */
    public List<ParteDiarioProduccion> partesDe(int idOrdenProduccion) {
        return parteDiarioDAO.listarPorOrden(idOrdenProduccion);
    }

    /**
     * Registra la inspeccion de calidad del lote (pasos 6 a 10).
     *
     * <p>El sistema valida el resultado: si la varianza de color supera la
     * tolerancia de la ficha tecnica, el lote se registra como Merma y el
     * progreso de la orden no avanza.
     */
    public ResultadoCalidad inspeccionar(InspeccionFormulario form, String codUsuario) {
        if (form == null || form.getIdLote() == null) {
            throw new ReglaNegocioException("Debe seleccionar el lote a inspeccionar.");
        }
        if (form.getVarianzaColorPct() == null) {
            throw new ReglaNegocioException("Debe registrar la varianza de color del lote.");
        }
        Lote lote = loteDAO.buscarPorId(form.getIdLote())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        if (!"PENDIENTE_INSPECCION".equals(lote.getEstado())) {
            throw new ReglaNegocioException("El lote " + lote.getCodigo() + " ya fue inspeccionado "
                    + "(estado " + lote.getEstado() + ").");
        }

        OrdenProduccion orden = ordenDAO.buscarPorId(lote.getIdOrdenProduccion())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        Pedido pedido = pedidoDAO.buscarPorId(orden.getIdPedido())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        FichaTecnica ficha = pedidoDAO.buscarFichaTecnica(pedido.getIdPedido()).orElse(null);

        BigDecimal tolerancia = ficha != null && ficha.getToleranciaColor() != null
                ? ficha.getToleranciaColor()
                : parametros.decimal("TOLERANCIA_COLOR_DEFAULT");

        // Regla RD7: la calidad se judgega contra la ficha tecnica.
        boolean conforme = form.getVarianzaColorPct().compareTo(tolerancia) <= 0;

        RegistroAvance avance = ultimoAvanceDe(lote.getIdAsignacion());

        InspeccionCalidad inspeccion = new InspeccionCalidad();
        inspeccion.setIdLote(lote.getIdLote());
        inspeccion.setIdAvance(avance == null ? null : avance.getIdAvance());
        inspeccion.setFecha(form.getFecha() == null ? LocalDate.now() : form.getFecha());
        inspeccion.setVarianzaColorPct(form.getVarianzaColorPct());
        inspeccion.setToleranciaAplicada(tolerancia);
        inspeccion.setResultado(conforme ? "CONFORME" : "NO_CONFORME");
        inspeccion.setObservacion(form.getObservacion());
        int idInspeccion = inspeccionDAO.insertar(inspeccion, codUsuario);

        ResultadoCalidad resultado = new ResultadoCalidad();
        resultado.setToleranciaAplicada(tolerancia);

        if (!conforme) {
            // Regla R-Merma: el lote se registra como Merma y el progreso no avanza.
            loteDAO.actualizarEstado(lote.getIdLote(), "MERMA");
            if (avance != null) {
                avanceDAO.actualizarEstadoCalidad(avance.getIdAvance(), "NO_CONFORME");
            }
            resultado.setConforme(false);
            resultado.setPorcentajeAvanceOrden(porcentajeAvance(orden));
            resultado.setMensaje("El lote " + lote.getCodigo() + " se registro como Merma: la "
                    + "varianza de color " + form.getVarianzaColorPct().toPlainString()
                    + " % supera la tolerancia de " + tolerancia.toPlainString()
                    + " % de la ficha tecnica. El lote debe reprocesarse y el progreso del "
                    + "pedido no avanza.");
        } else {
            loteDAO.actualizarEstado(lote.getIdLote(), "CONFORME");
            if (avance != null) {
                avanceDAO.actualizarEstadoCalidad(avance.getIdAvance(), "CONFORME");
            }
            BigDecimal metros = lote.getMetros() == null ? BigDecimal.ZERO : lote.getMetros();
            BigDecimal consolidado = orden.getMetrosConsolidados() == null ? BigDecimal.ZERO
                    : orden.getMetrosConsolidados();
            consolidado = consolidado.add(metros);
            ordenDAO.actualizarMetrosConsolidados(orden.getIdOrden(), consolidado);
            BigDecimal consolidadoPedido = pedido.getMetrosConsolidados() == null
                    ? BigDecimal.ZERO : pedido.getMetrosConsolidados();
            pedidoDAO.actualizarMetrosConsolidados(pedido.getIdPedido(),
                    consolidadoPedido.add(metros));

            BigDecimal porcentaje = porcentajeAvance(orden, consolidado);
            resultado.setConforme(true);
            resultado.setPorcentajeAvanceOrden(porcentaje);

            boolean cierre = porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0;
            if (cierre) {
                ordenDAO.actualizarEstado(orden.getIdOrden(), "TERMINADO");
                if (FabricaEstadoPedido.puedeTransitar(pedido.getEstado(), "TERMINADO")) {
                    pedidoDAO.actualizarEstado(pedido.getIdPedido(), "TERMINADO", codUsuario);
                }
                resultado.setCierreHabilitado(true);
                resultado.setMensaje("Lote conforme. El pedido " + pedido.getCodPedido()
                        + " alcanzo el 100 %: quedo habilitado el cierre de lote y el Parte "
                        + "Diario de Produccion.");
            } else {
                resultado.setMensaje("Lote conforme. El progreso de la orden es de "
                        + porcentaje.toPlainString() + " %.");
            }
        }

        auditoria.registrar(codUsuario, "PLANTA", "INSPECCION_CALIDAD", "inspeccion_calidad",
                String.valueOf(idInspeccion), "Lote " + lote.getCodigo() + " | Varianza: "
                        + form.getVarianzaColorPct().toPlainString() + " % | Tolerancia: "
                        + tolerancia.toPlainString() + " % | Resultado: "
                        + (conforme ? "CONFORME" : "NO_CONFORME"));
        return resultado;
    }

    /**
     * Genera el Parte Diario de Produccion (paso 11 del flujo).
     *
     * <p>Solo se habilita cuando el progreso de la orden alcanza el 100 %.
     */
    public ParteDiarioProduccion generarParteDiario(int idOrdenProduccion, LocalDate fecha,
            String codUsuario) {
        OrdenProduccion orden = ordenDAO.buscarPorId(idOrdenProduccion)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        BigDecimal consolidada = orden.getMetrosConsolidados() == null ? BigDecimal.ZERO
                : orden.getMetrosConsolidados();
        BigDecimal total = orden.getMetrosTotales() == null ? BigDecimal.ZERO
                : orden.getMetrosTotales();
        if (total.compareTo(BigDecimal.ZERO) <= 0
                || consolidada.compareTo(total) < 0) {
            throw new ReglaNegocioException("El Parte Diario se habilita cuando la Orden de "
                    + "Produccion " + orden.getCodOrden() + " alcanza el 100 % del metraje.");
        }

        BigDecimal horas = BigDecimal.ZERO;
        for (Asignacion asignacion : asignacionDAO.listarPorOrden(idOrdenProduccion)) {
            horas = horas.add(asignacion.getHorasAsignadas() == null ? BigDecimal.ZERO
                    : asignacion.getHorasAsignadas());
        }

        ParteDiarioProduccion parte = new ParteDiarioProduccion();
        parte.setIdOrdenProduccion(idOrdenProduccion);
        parte.setFecha(fecha == null ? LocalDate.now() : fecha);
        parte.setHorasHombre(horas);
        parte.setHorasMaquina(horas);
        parte.setMetros(consolidada);
        parte.setObservaciones("Cierre de lote de la orden " + orden.getCodOrden());
        parteDiarioDAO.guardar(parte, codUsuario);

        auditoria.registrar(codUsuario, "PLANTA", "GENERAR_PARTE_DIARIO", "parte_diario_produccion",
                String.valueOf(idOrdenProduccion), "Orden " + orden.getCodOrden() + " | Metros: "
                        + consolidada.toPlainString());
        return parte;
    }

    private RegistroAvance ultimoAvanceDe(int idAsignacion) {
        List<RegistroAvance> avances = avanceDAO.listarPorAsignacion(idAsignacion);
        return avances.isEmpty() ? null : avances.get(0);
    }

    private BigDecimal porcentajeAvance(OrdenProduccion orden) {
        return porcentajeAvance(orden, orden.getMetrosConsolidados() == null ? BigDecimal.ZERO
                : orden.getMetrosConsolidados());
    }

    private BigDecimal porcentajeAvance(OrdenProduccion orden, BigDecimal consolidado) {
        BigDecimal total = orden.getMetrosTotales() == null ? BigDecimal.ZERO
                : orden.getMetrosTotales();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return consolidado.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }
}
