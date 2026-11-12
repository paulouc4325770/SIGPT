package com.textilesandinos.sigpt.presentacion.comun;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.utilesweb.permisos.MenuHelper;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Atributos comunes a todas las vistas: el usuario autenticado y el menu de su
 * perfil.
 *
 * <p>El menu se arma una vez por peticion a partir de las opciones guardadas en
 * la sesion; la plantilla solo lo recorre.
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
}
