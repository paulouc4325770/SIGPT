package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Avance de pedidos: por estado, por cliente y por periodo, con el tablero de
 * control semaforizado.
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
    public List<String> columnas() {
        return List.of("Codigo", "Cliente", "Metros", "Consolidado", "% Avance",
                "Fecha de entrega", "Estado");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        List<List<Object>> filas = new ArrayList<>();
        for (PedidoFila pedido : pedidoDAO.listar(null, null, filtros.getIdCliente(),
                filtros.getFechaDesde(), filtros.getFechaHasta())) {
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
        ResumenPedidos resumen = pedidoDAO.resumir();
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Pedidos registrados",
                String.valueOf(resumen.getRegistrados()), "En espera de cotizacion"));
        indicadores.add(new Indicador("Cotizados", String.valueOf(resumen.getCotizados()),
                "A la espera del adelanto"));
        indicadores.add(new Indicador("En produccion", String.valueOf(resumen.getEnProduccion()),
                "Con Orden de Produccion emitida"));
        indicadores.add(new Indicador("Terminados", String.valueOf(resumen.getTerminados()),
                "Alcanzaron el 100 % del metraje"));
        indicadores.add(new Indicador("Anulados", String.valueOf(resumen.getAnulados()),
                "Cancelados antes de planta"));
        indicadores.add(new Indicador("Total", String.valueOf(resumen.getTotal()),
                "Pedidos registrados en el sistema"));
        return indicadores;
    }
}
