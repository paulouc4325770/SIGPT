package com.textilesandinos.sigpt.datos.planta;

import java.util.List;

import com.textilesandinos.sigpt.entidades.planta.Incidencia;

/**
 * Acceso a las incidencias de planta.
 */
public interface IncidenciaDAO {

    /** Inserta la incidencia y devuelve su identificador. */
    int insertar(Incidencia incidencia, String codUsuario);

    /** Incidencias registradas en una maquina. */
    List<Incidencia> listarPorMaquina(int idMaquina);

    /** Todas las incidencias, de la mas reciente a la mas antigua. */
    List<Incidencia> listarTodas();
}
