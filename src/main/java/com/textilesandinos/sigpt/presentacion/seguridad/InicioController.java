package com.textilesandinos.sigpt.presentacion.seguridad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Pantalla de inicio: recibe al usuario ya autenticado y muestra el tablero
 * con el menu de su perfil.
 */
@Controller
public class InicioController {

    @GetMapping({"/", "/inicio"})
    public String inicio(HttpSession sesion, Model modelo) {
        var usuario = SesionUsuario.usuario(sesion);
        if (usuario == null) {
            return "redirect:/seguridad/login";
        }
        modelo.addAttribute("titulo", "Inicio");
        return "inicio";
    }
}
