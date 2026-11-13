package com.textilesandinos.sigpt.negocio.reportes.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.OperarioDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.datos.planta.TurnoDAO;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;

/**
 * Productividad: metros por hora y horas-hombre por operario, maquina y turno.
 */
@Component
public class CalculoProductividad implements CalculoReporte {

    private final RegistroAvanceDAO avanceDAO;
    private final AsignacionDAO asignacionDAO;
    private final OperarioDAO operarioDAO;
    private final MaquinaDAO maquinaDAO;
    private final TurnoDAO turnoDAO;
    private final OrdenProduccionDAO ordenDAO;

    public CalculoProductividad(RegistroAvanceDAO avanceDAO, AsignacionDAO asignacionDAO,
            OperarioDAO operarioDAO, MaquinaDAO maquinaDAO, TurnoDAO turnoDAO,
            OrdenProduccionDAO ordenDAO) {
        this.avanceDAO = avanceDAO;
        this.asignacionDAO = asignacionDAO;
        this.operarioDAO = operarioDAO;
        this.maquinaDAO = maquinaDAO;
        this.turnoDAO = turnoDAO;
        this.ordenDAO = ordenDAO;
    }

    @Override
    public String codigo() {
        return "PRODUCTIVIDAD";
    }

    @Override
    public List<String> columnas() {
        return List.of("Operario", "Maquina", "Turno", "Metros producidos", "Horas",
                "Metros por hora");
    }

    @Override
    public List<List<Object>> filas(FiltrosReporte filtros) {
        List<List<Object>> filas = new ArrayList<>();
        for (OrdenFila orden : ordenDAO.listarEnProduccion()) {
            for (Asignacion asignacion : asignacionDAO.listarPorOrden(orden.getIdOrden())) {
                if (filtros.getIdOperario() != null
                        && !filtros.getIdOperario().equals(asignacion.getIdOperario())) {
                    continue;
                }
                if (filtros.getIdMaquina() != null
                        && !filtros.getIdMaquina().equals(asignacion.getIdMaquina())) {
                    continue;
                }
                BigDecimal metros = BigDecimal.ZERO;
                BigDecimal horas = BigDecimal.ZERO;
                for (RegistroAvance avance : avanceDAO.listarPorAsignacion(
                        asignacion.getIdAsignacion())) {
                    metros = metros.add(avance.getMetrosProducidos());
                    horas = horas.add(asignacion.getHorasAsignadas() == null ? BigDecimal.ZERO
                            : asignacion.getHorasAsignadas());
                }
                BigDecimal metrosHora = horas.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                        : metros.divide(horas, 2, RoundingMode.HALF_UP);
                filas.add(List.of(codigoOperario(asignacion.getIdOperario()),
                        codigoMaquina(asignacion.getIdMaquina()),
                        nombreTurno(asignacion.getIdTurno()),
                        metros.toPlainString(), horas.toPlainString(),
                        metrosHora.toPlainString()));
            }
        }
        return filas;
    }

    @Override
    public List<Indicador> indicadores(FiltrosReporte filtros) {
        List<List<Object>> filas = filas(filtros);
        BigDecimal metros = BigDecimal.ZERO;
        BigDecimal horas = BigDecimal.ZERO;
        for (List<Object> fila : filas) {
            metros = metros.add(new BigDecimal(fila.get(3).toString()));
            horas = horas.add(new BigDecimal(fila.get(4).toString()));
        }
        BigDecimal promedio = horas.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : metros.divide(horas, 2, RoundingMode.HALF_UP);
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Metros producidos", metros.toPlainString(),
                "Suma de los avances del periodo"));
        indicadores.add(new Indicador("Horas improductoras", horas.toPlainString(),
                "Horas asignadas a los operarios"));
        indicadores.add(new Indicador("Metros por hora", promedio.toPlainString(),
                "Promedio de la planta en el periodo"));
        indicadores.add(new Indicador("Registros", String.valueOf(filas.size()),
                "Combinaciones operario - maquina - turno"));
        return indicadores;
    }

    private String codigoOperario(int idOperario) {
        return operarioDAO.buscarPorId(idOperario)
                .map(operario -> operario.getCodigo()).orElse(String.valueOf(idOperario));
    }

    private String codigoMaquina(int idMaquina) {
        return maquinaDAO.buscarPorId(idMaquina)
                .map(maquina -> maquina.getCodigo()).orElse(String.valueOf(idMaquina));
    }

    private String nombreTurno(int idTurno) {
        return turnoDAO.buscarPorId(idTurno).map(turno -> turno.getNombre())
                .orElse(String.valueOf(idTurno));
    }
}
