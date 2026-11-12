package com.textilesandinos.sigpt.datos.seguridad;

import java.util.List;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;

/**
 * Acceso a los datos del menu. Las opciones son jerarquicas: {@code idOpcionRef}
 * apunta a la opcion padre.
 */
public interface OpcionDAO {

    /** Lista todas las opciones habilitadas de un perfil, ordenadas para el menu. */
    List<Opcion> listarPorPerfil(int idPerfil);

    /** Lista todas las opciones del sistema. */
    List<Opcion> listarTodas();

    java.util.Optional<Opcion> buscarPorId(int idOpcion);

    int insertar(Opcion opcion);

    int actualizar(Opcion opcion);

    boolean eliminar(int idOpcion);
}
