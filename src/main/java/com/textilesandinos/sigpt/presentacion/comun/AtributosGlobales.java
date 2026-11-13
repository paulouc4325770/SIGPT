package com.textilesandinos.sigpt.presentacion.comun;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.utilesweb.permisos.MenuHelper;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Atributos comunes a todas las vistas: el usuario autenticado y el menu de su
 * perfil.
 *
 * <p>El menu se arma una vez por peticion a partir de las opciones guardadas en
 * la sesion; la plantilla solo lo recorre.
 *
 * <p>Tambien resuelve el titulo de la pantalla a partir de la opcion de menu que
 * corresponde a la ruta solicitada, de modo que el encabezado y la pestaña del
 * navegador muestren el nombre de la pantalla sin que cada controlador tenga que
 * repetirlo.
 */
@ControllerAdvice
public class AtributosGlobales {

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(HttpSession sesion) {
        return SesionUsuario.usuario(sesion);
    }

    @ModelAttribute("menu")
    public Map<String, List<Opcion>> menu(HttpSession sesion) {
        return MenuHelper.construirMenu(SesionUsuario.opciones(sesion));
    }

    @ModelAttribute("nombreAplicacion")
    public String nombreAplicacion() {
        return "SIGPT";
    }

    /**
     * Titulo de la pantalla: el nombre de la opcion de menu cuya ruta
     * corresponde a la peticion actual (o {@code null} si la ruta no es una
     * opcion, por ejemplo una pagina de error).
     */
    @ModelAttribute("titulo")
    public String titulo(HttpServletRequest peticion, HttpSession sesion) {
        String ruta = peticion.getRequestURI();
        String contexto = peticion.getContextPath();
        if (contexto != null && !contexto.isEmpty() && ruta.startsWith(contexto)) {
            ruta = ruta.substring(contexto.length());
        }
        for (Opcion opcion : SesionUsuario.opciones(sesion)) {
            if (opcion.getUrlOpcion() == null || opcion.getUrlOpcion().isBlank()) {
                continue;
            }
            if (MenuHelper.tieneAcceso(List.of(opcion), ruta)) {
                return opcion.getNombreOpcion();
            }
        }
        return null;
    }
}
