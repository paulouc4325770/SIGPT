package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Eficiencia industrial: horas-hombre y horas-maquina reales frente a las
 * previstas en la cotizacion.
 */
@Component
public class CalculoEficiencia implements CalculoReporte {

    private final OrdenProduccionDAO ordenDAO;
    private final AsignacionDAO asignacionDAO;

    public CalculoEficiencia(OrdenProduccionDAO ordenDAO, AsignacionDAO asignacionDAO) {
        this.ordenDAO = ordenDAO;
        this.asignacionDAO = asignacionDAO;
    }

    @Override
    public String codigo() {
        return "EFICIENCIA_IND";
    }

    @Override
    public List<String> columnas() {
        return List.of("Orden", "Horas maquina previstas", "Horas maquina real", "Eficiencia",
                "Horas hombre previstas", "Horas hombre real", "Metros");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        List<List<Object>> filas = new ArrayList<>();
        for (OrdenFila orden : ordenDAO.listarEnProduccion()) {
            OrdenProduccion completa = ordenDAO.buscarPorId(orden.getIdOrden()).orElse(null);
            if (completa == null) {
                continue;
            }
            BigDecimal horasReales = BigDecimal.ZERO;
            for (Asignacion asignacion : asignacionDAO.listarPorOrden(orden.getIdOrden())) {
                horasReales = horasReales.add(asignacion.getHorasAsignadas() == null
                        ? BigDecimal.ZERO : asignacion.getHorasAsignadas());
            }
            BigDecimal previstas = completa.getHorasMaquinaPrevistas() == null ? BigDecimal.ZERO
                    : completa.getHorasMaquinaPrevistas();
            BigDecimal eficiencia = horasReales.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : previstas.multiply(BigDecimal.valueOf(100))
                            .divide(horasReales, 2, RoundingMode.HALF_UP);
            filas.add(List.of(orden.getCodOrden(),
                    previstas.toPlainString(), horasReales.toPlainString(),
                    eficiencia.toPlainString(),
                    String.valueOf(completa.getHorasHombrePrevistas()),
                    horasReales.toPlainString(),
                    String.valueOf(orden.getMetrosTotales())));
        }
        return filas;
    }

    @Override
    public List<Indicador> indicadores(FiltrosReporte filtros) {
        List<List<Object>> filas = filas(filtros);
        BigDecimal previstas = BigDecimal.ZERO;
        BigDecimal reales = BigDecimal.ZERO;
        for (List<Object> fila : filas) {
            previstas = previstas.add(new BigDecimal(fila.get(1).toString()));
            reales = reales.add(new BigDecimal(fila.get(2).toString()));
        }
        BigDecimal eficiencia = reales.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : previstas.multiply(BigDecimal.valueOf(100)).divide(reales, 2,
                        RoundingMode.HALF_UP);
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Ordenes en produccion", String.valueOf(filas.size()),
                "Con orden de produccion vigente"));
        indicadores.add(new Indicador("Horas maquina previstas", previstas.toPlainString(),
                "Segun la cotizacion aceptada"));
        indicadores.add(new Indicador("Horas maquina reales", reales.toPlainString(),
                "Horas asignadas en planta"));
        indicadores.add(new Indicador("Eficiencia", eficiencia.toPlainString() + " %",
                "Previstas sobre reales"));
        return indicadores;
    }
}
