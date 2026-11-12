package com.textilesandinos.sigpt.utilesweb.permisos;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

/**
 * Filtro de seguridad que se ejecuta en <b>cada</b> peticion.
 *
 * <p>Comprueba dos cosas:
 * <ol>
 *   <li>que exista una sesion iniciada, y</li>
 *   <li>que la ruta solicitada sea una de las opciones del perfil del usuario.</li>
 * </ol>
 *
 * <p>Es la traduccion de la regla de seguridad del enunciado: no basta con
 * ocultar la opcion del menu, hay que impedir el acceso directo a la URL.
 */
@Component
public class InterceptorPermisos implements HandlerInterceptor {

    /** Rutas que se pueden pedir sin sesion iniciada. */
    private static final List<String> RUTAS_PUBLICAS = List.of(
            "/seguridad/login",
            "/seguridad/logout",
            "/error",
            "/css/",
            "/js/",
            "/img/",
            "/favicon.ico");

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta,
            Object manejador) throws java.io.IOException {
        String ruta = peticion.getRequestURI();
        String contexto = peticion.getContextPath();
        if (contexto != null && !contexto.isEmpty() && ruta.startsWith(contexto)) {
            ruta = ruta.substring(contexto.length());
        }

        if (esPublica(ruta)) {
            return true;
        }

        if (!SesionUsuario.haySesion(peticion.getSession(false))) {
            respuesta.sendRedirect(contexto + "/seguridad/login?expira=1");
            return false;
        }

        List<Opcion> opciones = SesionUsuario.opciones(peticion.getSession(false));
        if (!MenuHelper.tieneAcceso(opciones, ruta)) {
            respuesta.sendError(HttpServletResponse.SC_FORBIDDEN, "Sin permisos para " + ruta);
            return false;
        }

        return true;
    }

    private boolean esPublica(String ruta) {
        for (String publica : RUTAS_PUBLICAS) {
            if (ruta.startsWith(publica)) {
                return true;
            }
        }
        return false;
    }
}
