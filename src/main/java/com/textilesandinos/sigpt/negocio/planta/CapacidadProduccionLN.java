package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.OperarioDAO;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.entidades.planta.Operario;

/**
 * Viabilidad operativa de un pedido (EF-1001-3, paso 4 del flujo).
 *
 * <p>La capacidad de telares se calcula con la velocidad teorica de cada
 * telar, las horas por turno y los tres turnos de la jornada.
 */
@Service
public class CapacidadProduccionLN {

    private static final int TURNOS_POR_JORNADA = 3;

    private final MaquinaDAO maquinaDAO;
    private final OperarioDAO operarioDAO;

    public CapacidadProduccionLN(MaquinaDAO maquinaDAO, OperarioDAO operarioDAO) {
        this.maquinaDAO = maquinaDAO;
        this.operarioDAO = operarioDAO;
    }

    /** Metros que la planta puede producir por dia sumando los telares. */
    public BigDecimal capacidadDiariaMetros() {
        BigDecimal capacidad = BigDecimal.ZERO;
        for (Maquina maquina : maquinaDAO.listarTodas()) {
            if (!maquina.getTipo().startsWith("TELAR")) {
                continue;
            }
            if ("MANTENIMIENTO".equals(maquina.getEstado())) {
                continue;
            }
            BigDecimal velocidad = maquina.getVelocidadTeoricaMtsHora();
            if (velocidad == null || velocidad.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal horas = maquina.getHorasPorTurno() == null ? BigDecimal.valueOf(8)
                    : maquina.getHorasPorTurno();
            capacidad = capacidad.add(
                    velocidad.multiply(horas).multiply(BigDecimal.valueOf(TURNOS_POR_JORNADA)));
        }
        return capacidad;
    }

    /** Operarios activos disponibles para la mano de obra. */
    public int operariosActivos() {
        List<Operario> operarios = operarioDAO.listarActivos();
        return operarios.size();
    }

    /**
     * Indica si el pedido es viable en el periodo solicitado.
     *
     * @param metros        metraje del pedido
     * @param fechaEntrega   fecha compromiso de entrega
     */
    public boolean esViable(BigDecimal metros, LocalDate fechaEntrega) {
        BigDecimal capacidadDiaria = capacidadDiariaMetros();
        if (capacidadDiaria.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        int dias = diasDisponibles(fechaEntrega);
        BigDecimal capacidad = capacidadDiaria.multiply(BigDecimal.valueOf(dias));
        return metros.compareTo(capacidad) <= 0;
    }

    /** Dias de produccion disponibles hasta la fecha de entrega. */
    public int diasDisponibles(LocalDate fechaEntrega) {
        if (fechaEntrega == null) {
            return 1;
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaEntrega);
        return (int) Math.max(1, Math.min(dias, 365));
    }
}
