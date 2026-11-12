package com.textilesandinos.sigpt.datos.comun;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapea una fila del {@link ResultSet} a un objeto del dominio.
 *
 * @param <T> tipo de la entidad
 */
@FunctionalInterface
public interface Mapeador<T> {

    T mapear(ResultSet resultado) throws SQLException;
}
