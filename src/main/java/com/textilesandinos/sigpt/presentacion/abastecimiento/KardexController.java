package com.textilesandinos.sigpt.presentacion.abastecimiento;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;
import com.textilesandinos.sigpt.negocio.abastecimiento.InsumoLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.KardexFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.KardexLN;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Kardex de movimientos de insumos (EF-1001-5).
 */
@Controller
public class KardexController {

    private final KardexLN kardexLN;
    private final InsumoLN insumoLN;

    public KardexController(KardexLN kardexLN, InsumoLN insumoLN) {
        this.kardexLN = kardexLN;
        this.insumoLN = insumoLN;
    }

    /** Kardex de movimientos. */
    @GetMapping("/abastecimiento/kardex")
    public String bandeja(Model modelo) {
        List<MovimientoKardex> movimientos = kardexLN.listarMovimientos();
        modelo.addAttribute("movimientos", movimientos);
        modelo.addAttribute("insumos", insumoLN.listar(null));
        modelo.addAttribute("kardexForm", new KardexFormulario());
        modelo.addAttribute("alertasStock", insumoLN.alertasStockMinimo());
        if (movimientos.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "abastecimiento/kardex";
    }

    /** Registra el movimiento de kardex. */
    @PostMapping("/abastecimiento/kardex/guardar")
    public String guardar(KardexFormulario formulario, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            kardexLN.registrar(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Movimiento registrado en el kardex");
            return "redirect:/abastecimiento/kardex";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/abastecimiento/kardex";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
