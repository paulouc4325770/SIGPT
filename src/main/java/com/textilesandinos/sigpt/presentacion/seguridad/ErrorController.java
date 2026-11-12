package com.textilesandinos.sigpt.presentacion.seguridad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Paginas de error propias (equivalente a las paginas 500 y 404 del laboratorio).
 * No se muestra detalle tecnico al usuario: el detalle queda en el log.
 */
@Controller
public class ErrorController {

    @GetMapping("/error/500")
    public String error500(Model modelo) {
        modelo.addAttribute("codigoError", 500);
        modelo.addAttribute("mensajeError",
                "Ocurrio un error inesperado. Intente mas tarde o contacte al soporte tecnico.");
        return "error/error";
    }

    @GetMapping("/error/404")
    public String error404(Model modelo) {
        modelo.addAttribute("codigoError", 404);
        modelo.addAttribute("mensajeError", "La pagina solicitada no existe.");
        return "error/error";
    }
}
