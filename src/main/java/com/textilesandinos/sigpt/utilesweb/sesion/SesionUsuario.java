package com.textilesandinos.sigpt.utilesweb.sesion;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;

/**
 * Constantes y utilidades de la sesion HTTP.
 *
 * <p>La sesion guarda el usuario autenticado y las opciones de menu de su
 * perfil. Es el equivalente del laboratorio, pero con las variables de la sesion
 * en lugar de una clase estatica global: nada que dependa del usuario vive en
 * un singleton.
 */
public final class SesionUsuario {

    /** Clave del atributo de sesion con el usuario autenticado. */
    public static final String ATRIBUTO_USUARIO = "gUsuario";

    /** Clave del atributo de sesion con las opciones de menu del perfil. */
    public static final String ATRIBUTO_OPCIONES = "gOpciones";

    private SesionUsuario() {
    }

    /** Recupera el usuario autenticado de la sesion, o {@code null}. */
    public static Usuario usuario(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ATRIBUTO_USUARIO);
        return valor instanceof Usuario usuario ? usuario : null;
    }

    /** Recupera las opciones de menu del perfil autenticado. */
    @SuppressWarnings("unchecked")
    public static List<Opcion> opciones(HttpSession sesion) {
        if (sesion == null) {
            return List.of();
        }
        Object valor = sesion.getAttribute(ATRIBUTO_OPCIONES);
        return valor instanceof List<?> lista ? (List<Opcion>) lista : List.of();
    }

    /** Indica si hay un usuario autenticado. */
    public static boolean haySesion(HttpSession sesion) {
        return usuario(sesion) != null;
    }
}
