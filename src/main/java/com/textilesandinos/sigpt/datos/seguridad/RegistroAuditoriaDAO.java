package com.textilesandinos.sigpt.datos.seguridad;

import java.util.List;

import com.textilesandinos.sigpt.entidades.seguridad.RegistroAuditoria;

/**
 * Acceso a la bitacora de auditoria (RC8): toda operacion critica deja traza
 * del codigo de usuario y de la marca de tiempo.
 */
public interface RegistroAuditoriaDAO {

    /** Registra una entrada de auditoria y devuelve su identificador. */
    int registrar(RegistroAuditoria registro);

    /** Lista las entradas de auditoria de un usuario, mas recientes primero. */
    List<RegistroAuditoria> listarPorUsuario(String codUsuario, int limite);

    /** Lista las entradas de auditoria de una entidad. */
    List<RegistroAuditoria> listarPorEntidad(String entidad, String idEntidad);

    /** Cuenta las entradas registradas. */
    int contar();
}
