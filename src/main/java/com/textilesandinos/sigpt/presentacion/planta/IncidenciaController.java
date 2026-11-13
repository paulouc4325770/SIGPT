package com.textilesandinos.sigpt.presentacion.planta;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.planta.Incidencia;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.negocio.planta.IncidenciaLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Incidencias de maquinaria (flujo 3.1 de EF-1001-7).
 */
@Controller
public class IncidenciaController {

    private final IncidenciaLN incidenciaLN;
    private final AsignacionLN asignacionLN;

    public IncidenciaController(IncidenciaLN incidenciaLN, AsignacionLN asignacionLN) {
        this.incidenciaLN = incidenciaLN;
        this.asignacionLN = asignacionLN;
    }

    /** Incidencias registradas. */
    @GetMapping("/planta/incidencias")
    public String bandeja(Model modelo) {
        List<Incidencia> incidencias = incidenciaLN.listar();
        modelo.addAttribute("incidencias", incidencias);
        modelo.addAttribute("maquinas", asignacionLN.maquinas());
        modelo.addAttribute("incidencia", new Incidencia());
        if (incidencias.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "planta/incidencias";
    }

    /** Registra una incidencia. */
    @PostMapping("/planta/incidencias/guardar")
    public String guardar(Incidencia incidencia, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            incidenciaLN.registrar(incidencia, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Incidencia registrada");
            return "redirect:/planta/incidencias";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/incidencias";
        }
    }

    /** Cierra una incidencia. */
    @PostMapping("/planta/incidencias/cerrar")
    public String cerrar(@RequestParam int idIncidencia, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            incidenciaLN.cerrar(idIncidencia, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Incidencia cerrada");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/planta/incidencias";
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
