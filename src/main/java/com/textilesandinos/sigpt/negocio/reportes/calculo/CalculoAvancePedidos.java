package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltroReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Avance de pedidos: por estado, por cliente y por periodo, con el tablero de
 * control semaforizado.
 *
 * <p>Los indicadores se calculan sobre los mismos pedidos que muestra el
 * detalle: si el usuario filtra por cliente o por fecha de entrega, el tablero
 * acompana al filtro en lugar de mostrar siempre los totales de la planta.
 */
@Component
public class CalculoAvancePedidos implements CalculoReporte {

    private final PedidoDAO pedidoDAO;

    public CalculoAvancePedidos(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    @Override
    public String codigo() {
        return "AVANCE_PEDIDOS";
    }

    @Override
    public List<FiltroReporte> filtrosSoportados() {
        return List.of(FiltroReporte.CLIENTE, FiltroReporte.FECHAS);
    }

    @Override
    public List<String> columnas() {
        return List.of("Codigo", "Cliente", "Metros", "Consolidado", "% Avance",
                "Fecha de entrega", "Estado");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        List<List<Object>> filas = new ArrayList<>();
        for (PedidoFila pedido : pedidos(filtros)) {
            BigDecimal consolidado = pedido.getMetrosConsolidados() == null ? BigDecimal.ZERO
                    : pedido.getMetrosConsolidados();
            BigDecimal metros = pedido.getMetros() == null ? BigDecimal.ZERO : pedido.getMetros();
            BigDecimal avance = metros.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : consolidado.multiply(BigDecimal.valueOf(100))
                            .divide(metros, 2, RoundingMode.HALF_UP);
            filas.add(List.of(pedido.getCodPedido(), pedido.getNombreCliente(),
                    metros.toPlainString(), consolidado.toPlainString(),
                    avance.toPlainString(), String.valueOf(pedido.getFechaEntrega()),
                    pedido.getEstado()));
        }
        return filas;
    }

    @Override
    public List<Indicador> indicadores(FiltrosReporte filtros) {
        List<PedidoFila> pedidos = pedidos(filtros);
        int registrados = 0;
        int cotizados = 0;
        int enProduccion = 0;
        int terminados = 0;
        int anulados = 0;
        for (PedidoFila pedido : pedidos) {
            switch (pedido.getEstado() == null ? "" : pedido.getEstado()) {
                case "REGISTRADO" -> registrados++;
                case "COTIZADO" -> cotizados++;
                case "EN_PRODUCCION" -> enProduccion++;
                case "TERMINADO" -> terminados++;
                case "ANULADO" -> anulados++;
                default -> { }
            }
        }
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Pedidos registrados", String.valueOf(registrados),
                "En espera de cotizacion"));
        indicadores.add(new Indicador("Cotizados", String.valueOf(cotizados),
                "A la espera del adelanto"));
        indicadores.add(new Indicador("En produccion", String.valueOf(enProduccion),
                "Con Orden de Produccion emitida"));
        indicadores.add(new Indicador("Terminados", String.valueOf(terminados),
                "Alcanzaron el 100 % del metraje"));
        indicadores.add(new Indicador("Anulados", String.valueOf(anulados),
                "Cancelados antes de planta"));
        indicadores.add(new Indicador("Total", String.valueOf(pedidos.size()),
                "Pedidos que cumplen los filtros"));
        return indicadores;
    }

    /**
     * Pedidos que cumplen los filtros del reporte.
     *
     * <p>El rango de fechas se aplica sobre la fecha de entrega comprometida.
     */
    private List<PedidoFila> pedidos(FiltrosReporte filtros) {
        return pedidoDAO.listar(null, null, filtros.getIdCliente(), filtros.getFechaDesde(),
                filtros.getFechaHasta());
    }
}
