package com.textilesandinos.sigpt.presentacion.planta;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.AsignacionFormulario;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Pantalla de asignacion de operarios y maquinaria (EF-1001-6).
 */
@Controller
public class AsignacionController {

    private final AsignacionLN asignacionLN;

    public AsignacionController(AsignacionLN asignacionLN) {
        this.asignacionLN = asignacionLN;
    }

    /** Tablero de asignacion. */
    @GetMapping("/planta/asignacion")
    public String tablero(@RequestParam(required = false) Integer orden, Model modelo) {
        List<OrdenFila> ordenes = asignacionLN.ordenesEnProduccion();
        modelo.addAttribute("ordenes", ordenes);
        modelo.addAttribute("operarios", asignacionLN.operarios());
        modelo.addAttribute("maquinas", asignacionLN.maquinas());
        modelo.addAttribute("turnos", asignacionLN.turnos());
        modelo.addAttribute("asignacionForm", new AsignacionFormulario());
        modelo.addAttribute("fechaAsignacion", LocalDate.now());
        modelo.addAttribute("ordenSeleccionada", null);

        if (orden != null) {
            for (OrdenFila fila : ordenes) {
                if (fila.getIdOrden().equals(orden)) {
                    modelo.addAttribute("ordenSeleccionada", fila);
                    break;
                }
            }
            List<Asignacion> asignaciones = asignacionLN.asignacionesDe(orden);
            modelo.addAttribute("asignaciones", asignaciones);
        } else {
            modelo.addAttribute("asignaciones", List.of());
        }

        if (ordenes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos", "No hay Ordenes de Produccion en estado "
                    + "En Produccion. La produccion se habilita cuando el adelanto verificado "
                    + "cubre el 40 % del valor pactado (Regla R2).");
        }
        return "planta/asignacion";
    }

    /** Registra la asignacion (pasos 5 a 7 del flujo de EF-1001-6). */
    @PostMapping("/planta/asignacion/guardar")
    public String guardar(AsignacionFormulario formulario, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            asignacionLN.asignar(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Asignacion registrada");
            return "redirect:/planta/asignacion";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/asignacion";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
