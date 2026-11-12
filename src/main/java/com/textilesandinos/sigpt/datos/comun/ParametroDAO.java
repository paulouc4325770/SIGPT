package com.textilesandinos.sigpt.datos.comun;

import java.util.List;

import com.textilesandinos.sigpt.entidades.comun.Parametro;

/**
 * Acceso a los parametros de negocio.
 */
public interface ParametroDAO {

    /** Lista los parametros habilitados. */
    List<Parametro> listarActivos();

    java.util.Optional<Parametro> buscarPorCodigo(String codigo);

    int insertar(Parametro parametro);

    int actualizar(Parametro parametro);
}
