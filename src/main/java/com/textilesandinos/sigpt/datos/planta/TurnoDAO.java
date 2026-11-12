package com.textilesandinos.sigpt.datos.planta;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.Turno;

/**
 * Acceso al catalogo de turnos de planta.
 */
public interface TurnoDAO {

    /** Lista los turnos: manana, tarde y amanecida. */
    List<Turno> listarTodos();

    /** Busca un turno por su identificador. */
    Optional<Turno> buscarPorId(int idTurno);
}
