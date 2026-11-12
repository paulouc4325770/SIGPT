package com.textilesandinos.sigpt.datos.planta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.Asignacion;

/**
 * Acceso a las asignaciones de operarios y maquinaria.
 */
public interface AsignacionDAO {

    /** Inserta la asignacion y devuelve su identificador. */
    int insertar(Asignacion asignacion, String codUsuario);

    /** Busca una asignacion por su identificador. */
    Optional<Asignacion> buscarPorId(int idAsignacion);

    /** Asignaciones de un operario en una fecha concreta. */
    List<Asignacion> listarPorOperarioYFecha(int idOperario, LocalDate fecha);

    /** Asignaciones de una maquina en una fecha concreta. */
    List<Asignacion> listarPorMaquinaYFecha(int idMaquina, LocalDate fecha);

    /** Asignaciones de una orden de produccion. */
    List<Asignacion> listarPorOrden(int idOrdenProduccion);

    /** Horas ya asignadas a un operario en una fecha (base de la Regla R-Turno). */
    BigDecimal horasAsignadas(int idOperario, LocalDate fecha);

    /** Cambia el estado de la asignacion. */
    int actualizarEstado(int idAsignacion, String estado);
}
