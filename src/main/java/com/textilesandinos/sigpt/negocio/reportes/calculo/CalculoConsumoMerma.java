package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.abastecimiento.InsumoDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.MovimientoKardexDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltroReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Consumo de insumos y merma, sobre el kardex valorizado.
 *
 * <p>El rango de fechas se aplica a los movimientos del kardex y a los avances
 * de planta: los ingresos y las salidas de la tabla son los del periodo, y la
 * merma es la declarada por los operarios en ese mismo periodo.
 */
@Component
public class CalculoConsumoMerma implements CalculoReporte {

    private final MovimientoKardexDAO kardexDAO;
    private final InsumoDAO insumoDAO;
    private final RegistroAvanceDAO avanceDAO;

    public CalculoConsumoMerma(MovimientoKardexDAO kardexDAO, InsumoDAO insumoDAO,
            RegistroAvanceDAO avanceDAO) {
        this.kardexDAO = kardexDAO;
        this.insumoDAO = insumoDAO;
        this.avanceDAO = avanceDAO;
    }

    @Override
    public String codigo() {
        return "CONSUMO_MERMA";
    }

    @Override
    public List<FiltroReporte> filtrosSoportados() {
        return List.of(FiltroReporte.FECHAS);
    }

    @Override
    public List<String> columnas() {
        return List.of("Insumo", "Unidad", "Ingresos del periodo", "Salidas del periodo",
                "Stock actual", "Valor del stock", "Stock minimo");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        List<MovimientoKardex> movimientos = movimientos(filtros);
        boolean filtrando = filtros.getFechaDesde() != null || filtros.getFechaHasta() != null;
        Map<Integer, BigDecimal[]> totales = new LinkedHashMap<>();
        for (MovimientoKardex movimiento : movimientos) {
            BigDecimal[] valores = totales.computeIfAbsent(movimiento.getIdInsumo(),
                    clave -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
            if ("INGRESO".equals(movimiento.getTipoMovimiento())) {
                valores[0] = valores[0].add(movimiento.getCantidad());
            } else if ("SALIDA".equals(movimiento.getTipoMovimiento())) {
                valores[1] = valores[1].add(movimiento.getCantidad());
            }
        }
        List<List<Object>> filas = new ArrayList<>();
        for (Insumo insumo : insumoDAO.listarTodos()) {
            BigDecimal[] valores = totales.get(insumo.getIdInsumo());
            if (valores == null && filtrando) {
                // Con un periodo elegido solo se listan los insumos con movimiento.
                continue;
            }
            if (valores == null) {
                valores = new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO};
            }
            BigDecimal valor = insumo.getStockActual() == null ? BigDecimal.ZERO
                    : insumo.getStockActual().multiply(insumo.getPrecioUnitario() == null
                            ? BigDecimal.ZERO : insumo.getPrecioUnitario());
            filas.add(List.of(insumo.getCodigo() + " - " + insumo.getNombre(),
                    insumo.getUnidadMedida(),
                    valores[0].toPlainString(), valores[1].toPlainString(),
                    String.valueOf(insumo.getStockActual()),
                    valor.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    String.valueOf(insumo.getStockMinimo())));
        }
        return filas;
    }

    @Override
    public List<Indicador> indicadores(FiltrosReporte filtros) {
        List<List<Object>> filas = filas(filtros);
        BigDecimal valorStock = BigDecimal.ZERO;
        for (List<Object> fila : filas) {
            valorStock = valorStock.add(new BigDecimal(fila.get(5).toString()));
        }
        BigDecimal producidos = BigDecimal.ZERO;
        BigDecimal merma = BigDecimal.ZERO;
        for (RegistroAvance avance : avanceDAO.listarEntreFechas(filtros.getFechaDesde(),
                filtros.getFechaHasta())) {
            producidos = producidos.add(avance.getMetrosProducidos() == null ? BigDecimal.ZERO
                    : avance.getMetrosProducidos());
            merma = merma.add(avance.getMetrosMerma() == null ? BigDecimal.ZERO
                    : avance.getMetrosMerma());
        }
        // R-Merma: la merma se mide contra lo que entro a produccion (producido + merma).
        BigDecimal total = producidos.add(merma);
        BigDecimal porcentajeMerma = total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : merma.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);

        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Valor del stock", valorStock.setScale(2).toPlainString(),
                "Stock actual valorizado a precio unitario"));
        indicadores.add(new Indicador("Merma del periodo",
                merma.setScale(2, RoundingMode.HALF_UP).toPlainString() + " m",
                porcentajeMerma.toPlainString() + " % de lo producido (R-Merma)"));
        indicadores.add(new Indicador("Insumos en stock minimo",
                String.valueOf(insumoDAO.listarEnStockMinimo().size()),
                "Requieren reposicion (R-Stock)"));
        indicadores.add(new Indicador("Movimientos de kardex",
                String.valueOf(movimientos(filtros).size()), "Ingresos y salidas del periodo"));
        indicadores.add(new Indicador("Insumos listados", String.valueOf(filas.size()),
                "Con movimiento en el periodo"));
        return indicadores;
    }

    /** Movimientos del kardex dentro del periodo pedido. */
    private List<MovimientoKardex> movimientos(FiltrosReporte filtros) {
        List<MovimientoKardex> movimientos = new ArrayList<>();
        for (MovimientoKardex movimiento : kardexDAO.listarTodos()) {
            if (filtros.enRango(movimiento.getFecha())) {
                movimientos.add(movimiento);
            }
        }
        return movimientos;
    }
}
