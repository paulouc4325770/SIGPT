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
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Consumo de insumos y merma, sobre el kardex valorizado.
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
    public List<String> columnas() {
        return List.of("Insumo", "Unidad", "Ingresos", "Salidas", "Stock actual",
                "Valor del stock", "Stock minimo");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        Map<Integer, BigDecimal[]> totales = new LinkedHashMap<>();
        for (MovimientoKardex movimiento : kardexDAO.listarTodos()) {
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
            BigDecimal[] valores = totales.getOrDefault(insumo.getIdInsumo(),
                    new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
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
        BigDecimal merma = BigDecimal.ZERO;
        for (List<Object> fila : filas) {
            valorStock = valorStock.add(new BigDecimal(fila.get(5).toString()));
        }
        for (var avance : avanceDAO.listarPorOrden(0)) {
            merma = merma.add(avance.getMetrosMerma() == null ? BigDecimal.ZERO
                    : avance.getMetrosMerma());
        }
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Valor del stock", valorStock.setScale(2).toPlainString(),
                "Stock actual valorizado a precio unitario"));
        indicadores.add(new Indicador("Insumos en stock minimo",
                String.valueOf(insumoDAO.listarEnStockMinimo().size()),
                "Requieren reposicion (R-Stock)"));
        indicadores.add(new Indicador("Movimientos de kardex",
                String.valueOf(kardexDAO.listarTodos().size()), "Ingresos y salidas registradas"));
        indicadores.add(new Indicador("Insumos catalogados", String.valueOf(filas.size()),
                "Insumos con movimiento en el kardex"));
        return indicadores;
    }
}
