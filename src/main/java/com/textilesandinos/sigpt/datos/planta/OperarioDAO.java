package com.textilesandinos.sigpt.datos.planta;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.planta.Operario;

/**
 * Acceso al padron de operarios de planta.
 */
public interface OperarioDAO {

    /** Lista los operarios activos. */
    List<Operario> listarActivos();

    /** Lista todos los operarios. */
    List<Operario> listarTodos();

    /** Busca un operario por su identificador. */
    Optional<Operario> buscarPorId(int idOperario);
}
