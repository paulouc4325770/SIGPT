package com.textilesandinos.sigpt.datos.seguridad;

import java.util.List;

import com.textilesandinos.sigpt.entidades.seguridad.Perfil;

/**
 * Acceso a los datos de los perfiles de acceso.
 */
public interface PerfilDAO {

    List<Perfil> listar();

    java.util.Optional<Perfil> buscarPorId(int idPerfil);

    java.util.Optional<Perfil> buscarPorCodigo(String codPerfil);

    int insertar(Perfil perfil);

    int actualizar(Perfil perfil);
}
