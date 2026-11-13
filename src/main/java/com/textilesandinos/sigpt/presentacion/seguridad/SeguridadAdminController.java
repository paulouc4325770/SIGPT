package com.textilesandinos.sigpt.presentacion.seguridad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.seguridad.SeguridadAdminLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Administracion del modulo de seguridad: usuarios, perfiles y matriz de
 * accesos (opciones de menu).
 */
@Controller
public class SeguridadAdminController {

    private final SeguridadAdminLN seguridadAdminLN;

    public SeguridadAdminController(SeguridadAdminLN seguridadAdminLN) {
        this.seguridadAdminLN = seguridadAdminLN;
    }

    /** Usuarios del sistema. */
    @GetMapping("/seguridad/usuarios")
    public String usuarios(Model modelo) {
        modelo.addAttribute("usuarios", seguridadAdminLN.listarUsuarios());
        modelo.addAttribute("perfiles", seguridadAdminLN.listarPerfiles());
        modelo.addAttribute("usuario", new Usuario());
        return "seguridad/usuarios";
    }

    /** Registra o actualiza un usuario. */
    @PostMapping("/seguridad/usuarios/guardar")
    public String guardarUsuario(Usuario usuario, @RequestParam(required = false) String clave,
            HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        try {
            boolean esNuevo = usuario.getIdUsuario() == null;
            if (esNuevo) {
                seguridadAdminLN.registrarUsuario(usuario, clave, usuarioActual(sesion));
                redireccion.addFlashAttribute("mensajeExito",
                        "Usuario registrado satisfactoriamente");
            } else {
                seguridadAdminLN.actualizarUsuario(usuario, usuarioActual(sesion));
                redireccion.addFlashAttribute("mensajeExito", "Usuario actualizado");
            }
            return "redirect:/seguridad/usuarios";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("usuarios", seguridadAdminLN.listarUsuarios());
            modelo.addAttribute("perfiles", seguridadAdminLN.listarPerfiles());
            modelo.addAttribute("usuario", usuario);
            return "seguridad/usuarios";
        }
    }

    /** Genera una clave nueva para un usuario. */
    @PostMapping("/seguridad/usuarios/clave")
    public String resetearClave(@RequestParam int idUsuario,
            @RequestParam(required = false) String clave, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            seguridadAdminLN.resetearClave(idUsuario, usuarioActual(sesion), clave);
            redireccion.addFlashAttribute("mensajeExito", "Clave actualizada");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/seguridad/usuarios";
    }

    /** Perfiles del sistema. */
    @GetMapping("/seguridad/perfiles")
    public String perfiles(Model modelo) {
        modelo.addAttribute("perfiles", seguridadAdminLN.listarPerfiles());
        modelo.addAttribute("matriz", seguridadAdminLN.listarMatrizAccesos());
        return "seguridad/perfiles";
    }

    /** Opciones del menu y matriz de accesos. */
    @GetMapping("/seguridad/opciones")
    public String opciones(Model modelo) {
        modelo.addAttribute("opciones", seguridadAdminLN.listarOpciones());
        modelo.addAttribute("perfiles", seguridadAdminLN.listarPerfiles());
        modelo.addAttribute("matriz", seguridadAdminLN.listarMatrizAccesos());
        return "seguridad/opciones";
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
