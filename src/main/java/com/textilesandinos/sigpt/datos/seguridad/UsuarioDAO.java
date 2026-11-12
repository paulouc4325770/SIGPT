package com.textilesandinos.sigpt.datos.seguridad;

import java.util.List;

import com.textilesandinos.sigpt.entidades.seguridad.Usuario;

/**
 * Acceso a los datos de los usuarios del sistema.
 *
 * <p>Solo sentencias parametrizadas: nunca se concatena texto en el SQL
 * (RDS5 / OWASP A03).
 */
public interface UsuarioDAO {

    /** Busca un usuario por su codigo, con su perfil cargado. */
    java.util.Optional<Usuario> buscarPorCodigo(String codUsuario);

    /** Busca un usuario por codigo y hash de contrasena. */
    java.util.Optional<Usuario> buscarPorCodigoYClaveHash(String codUsuario, String claveHash);

    /** Actualiza el contador de intentos fallidos y, si corresponde, el bloqueo. */
    int actualizarIntentos(int idUsuario, int intentosFallidos, java.time.LocalDateTime bloqueadoHasta);

    /** Registra el ultimo acceso del usuario. */
    int registrarAcceso(int idUsuario, java.time.LocalDateTime momento);

    /** Actualiza el hash de la contrasena. */
    int actualizarClave(int idUsuario, String claveHash);

    /** Lista todos los usuarios, con su perfil. */
    List<Usuario> listar();

    /** Inserta un usuario nuevo y devuelve su identificador. */
    int insertar(Usuario usuario);

    /** Actualiza los datos de un usuario. */
    int actualizar(Usuario usuario);

    /** Indica si ya existe un usuario con ese codigo. */
    boolean existePorCodigo(String codUsuario);

    /** Cuenta los usuarios registrados. */
    int contar();
}
