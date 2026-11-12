package com.textilesandinos.sigpt.presentacion.planta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.planta.Lote;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.negocio.planta.CalidadLN;
import com.textilesandinos.sigpt.negocio.planta.InspeccionFormulario;
import com.textilesandinos.sigpt.negocio.planta.ResultadoCalidad;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Pantalla de control de calidad (EF-1001-7, pasos 6 a 11).
 */
@Controller
public class CalidadController {

    private final CalidadLN calidadLN;
    private final AsignacionLN asignacionLN;

    public CalidadController(CalidadLN calidadLN, AsignacionLN asignacionLN) {
        this.calidadLN = calidadLN;
        this.asignacionLN = asignacionLN;
    }

    /** Lotes pendientes de inspeccion. */
    @GetMapping("/planta/calidad")
    public String calidad(@RequestParam(required = false) Integer orden, Model modelo) {
        List<Lote> lotes = calidadLN.lotesPendientes();
        List<OrdenFila> ordenes = asignacionLN.ordenesEnProduccion();
        modelo.addAttribute("lotes", lotes);
        modelo.addAttribute("ordenes", ordenes);
        modelo.addAttribute("inspeccion", new InspeccionFormulario());
        modelo.addAttribute("fechaInspeccion", LocalDate.now());

        List<ParteDiarioProduccion> partes = List.of();
        if (orden != null) {
            partes = calidadLN.partesDe(orden);
            modelo.addAttribute("lotesOrden", calidadLN.lotesDe(orden));
        } else {
            modelo.addAttribute("lotesOrden", List.of());
        }
        modelo.addAttribute("partes", partes);
        modelo.addAttribute("porcentajeAvance", new BigDecimal("0.00"));

        if (lotes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No hay lotes pendientes de inspeccion de calidad.");
        }
        return "planta/calidad";
    }

    /** Registra el resultado de la inspeccion (pasos 6 a 10). */
    @PostMapping("/planta/calidad/inspeccionar")
    public String inspeccionar(InspeccionFormulario inspeccion, HttpSession sesion,
            Model modelo, RedirectAttributes redireccion) {
        try {
            ResultadoCalidad resultado = calidadLN.inspeccionar(inspeccion,
                    usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", resultado.getMensaje());
            return "redirect:/planta/calidad";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/calidad";
        }
    }

    /** Genera el Parte Diario de Produccion (paso 11). */
    @PostMapping("/planta/calidad/parte-diario")
    public String parteDiario(@RequestParam int idOrden,
            @RequestParam(required = false) LocalDate fecha, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            ParteDiarioProduccion parte = calidadLN.generarParteDiario(idOrden, fecha,
                    usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Parte Diario generado: "
                    + parte.getMetros().toPlainString() + " m consolidados");
            return "redirect:/planta/calidad";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/calidad";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
