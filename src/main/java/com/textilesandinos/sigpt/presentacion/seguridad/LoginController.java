package com.textilesandinos.sigpt.presentacion.seguridad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.textilesandinos.sigpt.negocio.seguridad.ResultadoAutenticacion;
import com.textilesandinos.sigpt.negocio.seguridad.SeguridadLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador del inicio de sesion (EF-1001-2).
 *
 * <p>Al iniciar sesion correctamente se cargan en la sesion el usuario y las
 * opciones de menu de su perfil; con eso se arma el menu lateral.
 */
@Controller
public class LoginController {

    private final SeguridadLN seguridadLN;

    public LoginController(SeguridadLN seguridadLN) {
        this.seguridadLN = seguridadLN;
    }

    @GetMapping("/seguridad/login")
    public String mostrarLogin(Model modelo,
            @RequestParam(name = "expira", required = false) String expira) {
        if (expira != null) {
            modelo.addAttribute("mensajeError",
                    "Su sesion expiro por inactividad. Ingrese nuevamente.");
        }
        return "seguridad/login";
    }

    @PostMapping("/seguridad/login")
    public String procesarLogin(@RequestParam("codUsuario") String codUsuario,
            @RequestParam("clave") String clave, HttpSession sesion, Model modelo) {
        ResultadoAutenticacion resultado = seguridadLN.autenticar(codUsuario, clave);

        if (!resultado.esExitoso()) {
            modelo.addAttribute("mensajeError", resultado.getMensaje());
            modelo.addAttribute("codUsuario", codUsuario);
            return "seguridad/login";
        }

        sesion.setAttribute(SesionUsuario.ATRIBUTO_USUARIO, resultado.getUsuario());
        sesion.setAttribute(SesionUsuario.ATRIBUTO_OPCIONES, resultado.getOpciones());
        sesion.setMaxInactiveInterval(15 * 60);

        return "redirect:/inicio";
    }

    @GetMapping("/seguridad/logout")
    public String cerrarSesion(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        if (usuario != null) {
            seguridadLN.registrarCierreSesion(usuario.getCodUsuario());
        }
        sesion.invalidate();
        return "redirect:/seguridad/login";
    }
}
