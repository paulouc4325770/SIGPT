package com.textilesandinos.sigpt.datos.planta;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.Maquina;

/**
 * Acceso al parque de maquinas de planta.
 */
public interface MaquinaDAO {

    /** Lista todas las maquinas con su estado actual. */
    List<Maquina> listarTodas();

    /** Busca una maquina por su identificador. */
    Optional<Maquina> buscarPorId(int idMaquina);

    /** Cambia el estado de la maquina (LIBRE, OCUPADA, MANTENIMIENTO, BACKUP). */
    int actualizarEstado(int idMaquina, String estado);
}
