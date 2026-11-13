package com.textilesandinos.sigpt.presentacion.planta;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.CalidadLN;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Parte Diario de Produccion (paso 11 de EF-1001-7).
 */
@Controller
public class ParteDiarioController {

    private final CalidadLN calidadLN;
    private final AsignacionLN asignacionLN;

    public ParteDiarioController(CalidadLN calidadLN, AsignacionLN asignacionLN) {
        this.calidadLN = calidadLN;
        this.asignacionLN = asignacionLN;
    }

    /** Partes diarios registrados. */
    @GetMapping("/planta/parte-diario")
    public String bandeja(@RequestParam(required = false) Integer orden, Model modelo) {
        List<OrdenFila> ordenes = asignacionLN.ordenesEnProduccion();
        modelo.addAttribute("ordenes", ordenes);
        List<ParteDiarioProduccion> partes = orden == null ? List.of()
                : calidadLN.partesDe(orden);
        modelo.addAttribute("partes", partes);
        modelo.addAttribute("ordenSeleccionada", orden);
        if (partes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "Todavia no hay partes diarios para la orden seleccionada.");
        }
        return "planta/parte-diario";
    }

    /** Genera el parte diario de la orden. */
    @PostMapping("/planta/parte-diario/guardar")
    public String guardar(@RequestParam int idOrden, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            ParteDiarioProduccion parte = calidadLN.generarParteDiario(idOrden, LocalDate.now(),
                    usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Parte Diario generado: "
                    + parte.getMetros().toPlainString() + " m consolidados");
            return "redirect:/planta/parte-diario?orden=" + idOrden;
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/parte-diario";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
